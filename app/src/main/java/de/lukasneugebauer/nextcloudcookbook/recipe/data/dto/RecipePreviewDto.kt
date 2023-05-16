package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview

data class RecipePreviewDto(
    @SerializedName("recipe_id")
    val recipeId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("keywords")
    val keywords: String?,
    @SerializedName("dateCreated")
    val dateCreated: String,
    @SerializedName("dateModified")
    val dateModified: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("imagePlaceholderUrl")
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
