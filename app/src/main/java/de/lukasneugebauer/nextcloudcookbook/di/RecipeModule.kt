package de.lukasneugebauer.nextcloudcookbook.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.repository.RecipeRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

typealias RecipePreviewsByCategoryStore = Store<String, List<RecipePreviewDto>>
typealias RecipePreviewsStore = Store<Any, List<RecipePreviewDto>>
typealias RecipeStore = Store<Int, RecipeDto>

@Module
@InstallIn(SingletonComponent::class)
object RecipeModule {

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipePreviewsByCategoryStore(apiProvider: ApiProvider): RecipePreviewsByCategoryStore {
        val ncCookbookApi = apiProvider.getNcCookbookApi()
            ?: throw NullPointerException("Nextcloud Cookbook API is null.")
        return StoreBuilder
            .from(
                fetcher = Fetcher.of { categoryName: String ->
                    ncCookbookApi.getRecipesByCategory(categoryName)
                }
            )
            .build()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipePreviewsStore(apiProvider: ApiProvider): RecipePreviewsStore {
        val ncCookbookApi = apiProvider.getNcCookbookApi()
            ?: throw NullPointerException("Nextcloud Cookbook API is null.")
        return StoreBuilder
            .from(Fetcher.of { ncCookbookApi.getRecipes() })
            .build()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipeStore(apiProvider: ApiProvider): RecipeStore {
        val ncCookbookApi = apiProvider.getNcCookbookApi()
            ?: throw NullPointerException("Nextcloud Cookbook API is null.")
        return StoreBuilder
            .from(Fetcher.of { recipeId: Int -> ncCookbookApi.getRecipe(recipeId) })
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        apiProvider: ApiProvider,
        recipesByCategoryStore: RecipePreviewsByCategoryStore,
        recipePreviewsStore: RecipePreviewsStore,
        recipeStore: RecipeStore
    ): RecipeRepository =
        RecipeRepositoryImpl(
            apiProvider,
            recipesByCategoryStore,
            recipePreviewsStore,
            recipeStore
        )
}