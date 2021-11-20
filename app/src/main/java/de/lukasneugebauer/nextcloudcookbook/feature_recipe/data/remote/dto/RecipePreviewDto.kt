package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

data class RecipePreviewDto(
    @SerializedName("recipe_id") val recipeId: String,
    val name: String,
    val keywords: String?,
    val dateCreated: String,
    val dateModified: String,
    val imageUrl: String,
    val imagePlaceholderUrl: String,
) {
    fun toRecipePreview() = RecipePreview(
        id = recipeId.toInt(),
        name = name,
        keywords = keywords?.split(",") ?: emptyList(),
        imageUrl = imageUrl,
        createdAt = dateCreated,
        modifiedAt = dateModified,
    )
}