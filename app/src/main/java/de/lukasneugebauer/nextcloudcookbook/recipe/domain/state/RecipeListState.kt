package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview

data class RecipeListState(
    val loading: Boolean = true,
    val data: List<RecipePreview> = emptyList()
)
