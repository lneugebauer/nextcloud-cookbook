package de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllKeywordsUseCase
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) {
        operator fun invoke(filterByCategory: String? = null): Flow<Set<String>> {
            val recipePreviewsFlow =
                if (filterByCategory.isNullOrBlank()) {
                    recipeRepository.getRecipePreviewsFlow()
                } else {
                    recipeRepository.getRecipePreviewsByCategory(filterByCategory)
                }

            return recipePreviewsFlow.mapNotNull { recipePreviewsResult ->
                when (recipePreviewsResult) {
                    is DataResult.Success -> recipePreviewsResult.data.flatMap { it.keywords }.toSet()
                    else -> null
                }
            }
        }
    }
