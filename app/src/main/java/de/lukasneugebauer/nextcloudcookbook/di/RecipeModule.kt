package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import coil3.imageLoader
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
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import javax.inject.Singleton

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
    fun provideRecipePreviewsByCategoryStore(apiProvider: NcCookbookApiProvider): RecipePreviewsByCategoryStore =
        StoreBuilder
            .from(
                Fetcher.of { categoryName: String ->
                    apiProvider.getApi()?.getRecipesByCategory(categoryName)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
            ).build()

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipePreviewsStore(apiProvider: NcCookbookApiProvider): RecipePreviewsStore =
        StoreBuilder
            .from(
                Fetcher.of {
                    apiProvider.getApi()?.getRecipes()
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
            ).build()

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipeStore(apiProvider: NcCookbookApiProvider): RecipeStore =
        StoreBuilder
            .from(
                Fetcher.of { recipeId: String ->
                    apiProvider.getApi()?.getRecipe(recipeId)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
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
