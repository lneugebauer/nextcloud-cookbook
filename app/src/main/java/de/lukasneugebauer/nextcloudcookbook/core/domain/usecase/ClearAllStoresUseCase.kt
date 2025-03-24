package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import javax.inject.Inject

class ClearAllStoresUseCase
    @Inject
    constructor(
        private val categoriesStore: CategoriesStore,
        private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
    ) {
        @OptIn(ExperimentalStoreApi::class)
        suspend operator fun invoke() {
            listOf(
                categoriesStore,
                recipePreviewsByCategoryStore,
                recipePreviewsStore,
                recipeStore,
            ).forEach { store ->
                store.clear()
            }
        }
    }
