package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import coil3.imageLoader
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.recipe.data.RecipeFormatterImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.repository.RecipeRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.RecipeFormatter
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.YieldCalculator
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipePreviewDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import javax.inject.Singleton
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.mapper.toDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.mapper.toEntity

typealias RecipePreviewsByCategoryStore = Store<String, List<RecipePreviewDto>>
typealias RecipePreviewsStore = Store<Any, List<RecipePreviewDto>>
typealias RecipeStore = Store<String, RecipeDto>


@Module
@InstallIn(SingletonComponent::class)
object RecipeModule {
    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipePreviewsStore(
        apiProvider: NcCookbookApiProvider,
        recipePreviewDao: RecipePreviewDao,
    ): RecipePreviewsStore =
        StoreBuilder
            .from(
                fetcher = Fetcher.of {
                    apiProvider.getApi()?.getRecipes()
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
                sourceOfTruth = SourceOfTruth.of<Any, List<RecipePreviewDto>, List<RecipePreviewDto>>(
                    reader = { _: Any ->
                        recipePreviewDao.getAll().map { entities ->
                            entities.map { it.toDto() }.takeIf { it.isNotEmpty() }
                        }
                    },
                    writer = { _: Any, dtos: List<RecipePreviewDto> ->
                        recipePreviewDao.deleteAll()
                        recipePreviewDao.upsertAll(dtos.map { it.toEntity() })
                    },
                    delete = { recipePreviewDao.deleteAll() },
                    deleteAll = { recipePreviewDao.deleteAll() },
                ),
            ).build()

    @Provides
    @Singleton
    fun provideRecipePreviewsByCategoryStore(
        apiProvider: NcCookbookApiProvider,
        recipePreviewDao: RecipePreviewDao,
    ): RecipePreviewsByCategoryStore =
        StoreBuilder
            .from(
                fetcher = Fetcher.of { categoryName: String ->
                    apiProvider.getApi()?.getRecipesByCategory(categoryName)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
                sourceOfTruth = SourceOfTruth.of<String, List<RecipePreviewDto>, List<RecipePreviewDto>>(
                    reader = { categoryName: String ->
                        recipePreviewDao.getByCategory(categoryName).map { entities ->
                            entities.map { it.toDto() }.takeIf { it.isNotEmpty() }
                        }
                    },
                    writer = { _: String, dtos: List<RecipePreviewDto> ->
                        recipePreviewDao.upsertAll(dtos.map { it.toEntity() })
                    },
                    delete = { recipePreviewDao.deleteAll() },
                    deleteAll = { recipePreviewDao.deleteAll() },
                ),
            ).build()

    @Provides
    @Singleton
    fun provideRecipeStore(
        apiProvider: NcCookbookApiProvider,
        recipeDao: RecipeDao,
        gson: Gson,
    ): RecipeStore =
        StoreBuilder
            .from(
                fetcher = Fetcher.of { recipeId: String ->
                    apiProvider.getApi()?.getRecipe(recipeId)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
                sourceOfTruth = SourceOfTruth.of<String, RecipeDto, RecipeDto>(
                    reader = { id: String ->
                        recipeDao.getById(id).map { entity ->
                            entity?.let { gson.fromJson(it.json, RecipeDto::class.java) }
                        }
                    },
                    writer = { _: String, dto: RecipeDto ->
                        recipeDao.upsert(RecipeEntity(id = dto.id, json = gson.toJson(dto)))
                    },
                    delete = { id: String -> recipeDao.deleteById(id) },
                    deleteAll = { recipeDao.deleteAll() },
                ),
            ).build()

    @Provides
    @Singleton
    fun provideRecipeRepository(
        apiProvider: NcCookbookApiProvider,
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        recipesByCategoryStore: RecipePreviewsByCategoryStore,
        recipePreviewsStore: RecipePreviewsStore,
        recipeStore: RecipeStore,
    ): RecipeRepository =
        RecipeRepositoryImpl(
            apiProvider,
            context.imageLoader,
            ioDispatcher,
            recipesByCategoryStore,
            recipePreviewsStore,
            recipeStore,
        )

    @Provides
    @Singleton
    fun provideRecipeFormatter(
        @ApplicationContext context: Context,
    ): RecipeFormatter = RecipeFormatterImpl(resources = context.resources)

    @Provides
    @Singleton
    fun provideYieldCalculator(): YieldCalculator = YieldCalculatorImpl()
}
