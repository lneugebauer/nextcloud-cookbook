package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

data class RecipePreview(
    val id: String,
    val name: String,
    val keywords: Set<String>,
    val category: String,
    val imageUrl: String,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
)
