package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

data class RecipeDetailState(
    val data: Recipe? = null,
    val deleted: Boolean = false,
    val error: UiText? = null,
    val loading: Boolean = true
)
