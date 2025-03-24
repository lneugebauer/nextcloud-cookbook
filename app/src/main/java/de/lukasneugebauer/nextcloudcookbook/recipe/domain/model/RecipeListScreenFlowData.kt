package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import org.mobilenativefoundation.store.store5.StoreReadResponse

data class RecipeListScreenFlowData(
    val recipePreviewsResponse: StoreReadResponse<List<RecipePreviewDto>>,
    val query: String,
    val selectedKeyword: List<String>,
    val order: RecipeListScreenOrder,
)
