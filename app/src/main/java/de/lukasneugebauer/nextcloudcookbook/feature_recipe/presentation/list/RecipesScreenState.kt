package de.lukasneugebauer.nextcloudcookbook.ui.recipes

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

data class RecipesScreenState(
    val data: List<RecipePreview>
)