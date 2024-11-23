package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto

data class RecipeListScreenFlowData(
    val recipePreviewsResponse: StoreResponse<List<RecipePreviewDto>>,
    val query: String,
    val selectedKeyword: List<String>,
    val order: RecipeListScreenOrder,
)
