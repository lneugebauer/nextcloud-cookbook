package de.lukasneugebauer.nextcloudcookbook.data.model

import de.lukasneugebauer.nextcloudcookbook.domain.model.Category

data class CategoryNw(
    val name: String,
    val recipe_count: Int,
) {
    fun toCategory() = Category(
        name = name,
        recipeCount = recipe_count,
    )
}