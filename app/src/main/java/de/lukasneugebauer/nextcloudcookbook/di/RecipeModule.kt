package de.lukasneugebauer.nextcloudcookbook.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.data.model.CategoryNw
import de.lukasneugebauer.nextcloudcookbook.data.model.RecipeNw
import de.lukasneugebauer.nextcloudcookbook.data.model.RecipePreviewNw
import de.lukasneugebauer.nextcloudcookbook.data.repository.RecipeRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

typealias CategoriesStore = Store<Any, List<CategoryNw>>
typealias RecipePreviewsByCategoryStore = Store<String, List<RecipePreviewNw>>
typealias RecipePreviewsStore = Store<Any, List<RecipePreviewNw>>
typealias RecipeStore = Store<Int, RecipeNw>

@Module
@InstallIn(SingletonComponent::class)
object RecipeModule {

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideCategories(apiProvider: ApiProvider): CategoriesStore {
        val ncCookbookApi = apiProvider.getNcCookbookApi()
            ?: throw NullPointerException("Nextcloud Cookbook API is null.")
        return StoreBuilder
            .from(Fetcher.of { ncCookbookApi.getCategories() })
            .build()
    }

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
        categoriesStore: CategoriesStore,
        recipesByCategoryStore: RecipePreviewsByCategoryStore,
        recipePreviewsStore: RecipePreviewsStore,
        recipeStore: RecipeStore
    ): RecipeRepository =
        RecipeRepositoryImpl(
            categoriesStore,
            recipesByCategoryStore,
            recipePreviewsStore,
            recipeStore
        )
}