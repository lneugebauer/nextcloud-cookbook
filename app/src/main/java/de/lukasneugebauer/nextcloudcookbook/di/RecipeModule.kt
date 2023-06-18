package de.lukasneugebauer.nextcloudcookbook.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.repository.RecipeRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
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
        return StoreBuilder
            .from(
                Fetcher.of { categoryName: String ->
                    apiProvider.getNcCookbookApi()?.getRecipesByCategory(categoryName)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
            )
            .build()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipePreviewsStore(apiProvider: ApiProvider): RecipePreviewsStore {
        return StoreBuilder
            .from(
                Fetcher.of {
                    apiProvider.getNcCookbookApi()?.getRecipes()
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
            )
            .build()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideRecipeStore(apiProvider: ApiProvider): RecipeStore {
        return StoreBuilder
            .from(
                Fetcher.of { recipeId: Int ->
                    apiProvider.getNcCookbookApi()?.getRecipe(recipeId)
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        apiProvider: ApiProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        recipesByCategoryStore: RecipePreviewsByCategoryStore,
        recipePreviewsStore: RecipePreviewsStore,
        recipeStore: RecipeStore,
    ): RecipeRepository =
        RecipeRepositoryImpl(
            apiProvider,
            ioDispatcher,
            recipesByCategoryStore,
            recipePreviewsStore,
            recipeStore,
        )
}
