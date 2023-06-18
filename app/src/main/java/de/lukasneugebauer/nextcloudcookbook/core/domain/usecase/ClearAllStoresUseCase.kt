package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import com.dropbox.android.external.store4.ExperimentalStoreApi
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import javax.inject.Inject

@OptIn(ExperimentalStoreApi::class)
class ClearAllStoresUseCase @Inject constructor(
    private val categoriesStore: CategoriesStore,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore,
) {

    suspend operator fun invoke() {
        listOf(
            categoriesStore,
            recipePreviewsByCategoryStore,
            recipePreviewsStore,
            recipeStore,
        ).forEach { store ->
            store.clearAll()
        }
    }
}
