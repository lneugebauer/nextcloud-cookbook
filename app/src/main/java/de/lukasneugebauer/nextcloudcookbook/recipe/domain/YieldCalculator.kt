package de.lukasneugebauer.nextcloudcookbook.recipe.domain

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CalculatedIngredient

interface YieldCalculator {
    fun isValidIngredientSyntax(ingredient: String): Boolean

    fun recalculateIngredients(
        ingredients: List<String>,
        currentYield: Int,
        originalYield: Int,
    ): List<CalculatedIngredient>
}
