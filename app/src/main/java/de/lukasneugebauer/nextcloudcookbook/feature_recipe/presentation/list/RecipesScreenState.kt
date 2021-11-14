package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

data class RecipesScreenState(
    val data: List<RecipePreview> = emptyList()
)