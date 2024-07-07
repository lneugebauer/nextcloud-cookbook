package de.lukasneugebauer.nextcloudcookbook.recipe.domain

interface YieldCalculator {
    fun isValidIngredientSyntax(ingredient: String): Boolean

    fun recalculateIngredients(
        ingredients: List<String>,
        currentYield: Int,
        originalYield: Int,
    ): List<String>
}
