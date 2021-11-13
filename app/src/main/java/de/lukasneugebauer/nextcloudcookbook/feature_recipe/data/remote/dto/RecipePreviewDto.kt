package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

data class RecipePreviewDto(
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