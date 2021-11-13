package de.lukasneugebauer.nextcloudcookbook.feature_category.data.remote.dto

import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category

data class CategoryDto(
    val name: String,
    val recipe_count: Int,
) {
    fun toCategory() = Category(
        name = name,
        recipeCount = recipe_count,
    )
}