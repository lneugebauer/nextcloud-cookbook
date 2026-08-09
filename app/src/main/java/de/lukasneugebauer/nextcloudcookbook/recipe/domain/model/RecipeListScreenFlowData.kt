package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult

data class RecipeListScreenFlowData(
    val recipePreviewsResult: DataResult<List<RecipePreview>>,
    val query: String,
    val selectedKeyword: List<String>,
    val order: RecipeListScreenOrder,
)
