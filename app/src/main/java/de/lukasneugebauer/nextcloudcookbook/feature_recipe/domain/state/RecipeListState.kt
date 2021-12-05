package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

data class RecipeListState(
    val loading: Boolean = true,
    val data: List<RecipePreview> = emptyList()
)