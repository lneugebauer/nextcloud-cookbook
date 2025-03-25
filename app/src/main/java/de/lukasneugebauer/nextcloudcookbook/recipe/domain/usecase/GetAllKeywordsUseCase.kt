package de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.store5.StoreReadResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllKeywordsUseCase
    @Inject
    constructor(private val recipeRepository: RecipeRepository) {
        operator fun invoke(filterByCategory: String? = null): Flow<Set<String>> {
            val recipePreviewsFlow =
                if (filterByCategory.isNullOrBlank()) {
                    recipeRepository.getRecipePreviewsFlow()
                } else {
                    recipeRepository.getRecipePreviewsByCategory(filterByCategory)
                }

            return recipePreviewsFlow.mapNotNull { recipePreviewsResponse ->
                when (recipePreviewsResponse) {
                    is StoreReadResponse.Data -> {
                        recipePreviewsResponse.value
                            .flatMap { it.toRecipePreview().keywords }
                            .toSet()
                    }

                    else -> null
                }
            }
        }
    }
