package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

data class RecipeDetailState(
    val data: Recipe? = null,
    val error: String? = null,
    val loading: Boolean = true
)