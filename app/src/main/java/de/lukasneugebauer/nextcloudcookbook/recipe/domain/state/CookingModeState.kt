package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction

data class CookingModeState(
    val recipeName: String = "",
    val instructions: List<Instruction> = emptyList(),
    val loading: Boolean = true,
    val error: UiText? = null,
)
