package de.lukasneugebauer.nextcloudcookbook.data.models.category

data class CategoryNw(
    val name: String,
    val recipe_count: Int,
) {
    fun toCategory() = Category(
        name = name,
        recipeCount = recipe_count,
    )
}

data class Category(
    val name: String,
    val recipeCount: Int,
)