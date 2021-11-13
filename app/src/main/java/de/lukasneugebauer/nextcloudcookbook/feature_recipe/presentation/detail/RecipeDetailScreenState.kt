package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

data class RecipeDetailScreenState(
    val data: Recipe? = null,
    val error: String? = null,
    val loading: Boolean = true
)