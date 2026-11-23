package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction

data class CookingModeState(
    val recipeName: String = "",
    val instructions: List<Instruction> = emptyList(),
    /** Ingredients recalculated for [servings]. */
    val ingredients: List<String> = emptyList(),
    /** The servings the ingredients are shown for; below 1 if the recipe does not state a yield. */
    val servings: Int = 0,
    val loading: Boolean = true,
    val error: UiText? = null,
)
