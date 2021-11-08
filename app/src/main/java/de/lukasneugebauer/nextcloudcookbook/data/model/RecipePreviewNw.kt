package de.lukasneugebauer.nextcloudcookbook.data.model

import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview

data class RecipePreviewNw(
    val recipe_id: String,
    val name: String,
    val keywords: String?,
    val dateCreated: String,
    val dateModified: String,
    val imageUrl: String,
    val imagePlaceholderUrl: String,
) {
    fun toRecipePreview() = RecipePreview(
        id = recipe_id.toInt(),
        name = name,
        keywords = keywords?.split(",") ?: emptyList(),
        imageUrl = imageUrl,
        createdAt = dateCreated,
        modifiedAt = dateModified,
    )
}