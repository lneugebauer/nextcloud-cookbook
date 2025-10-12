package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CalculatedIngredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe

data class RecipeDetailState(
    val data: Recipe? = null,
    val calculatedIngredients: List<CalculatedIngredient> = emptyList(),
    val currentYield: Int = 0,
    val deleted: Boolean = false,
    val error: UiText? = null,
    val loading: Boolean = true,
)
