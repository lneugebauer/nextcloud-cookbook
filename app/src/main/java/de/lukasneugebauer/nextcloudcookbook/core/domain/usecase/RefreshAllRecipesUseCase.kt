package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import javax.inject.Inject

class RefreshAllRecipesUseCase
    @Inject
    constructor(
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
    ) {
        @OptIn(ExperimentalStoreApi::class)
        suspend operator fun invoke() {
            // Clear stores so that previously cached data is discarded
            listOf(
                recipePreviewsStore,
                recipeStore,
            ).forEach { store ->
                store.clear()
            }

            // Force fetching fresh previews (key = Unit)
            recipePreviewsStore.fresh(Unit)
        }
    }
