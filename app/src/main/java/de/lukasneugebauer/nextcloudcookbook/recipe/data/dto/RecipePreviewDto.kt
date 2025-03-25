package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview

data class RecipePreviewDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("keywords")
    val keywords: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("dateCreated")
    val dateCreated: String?,
    @SerializedName("dateModified")
    val dateModified: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("imagePlaceholderUrl")
    val imagePlaceholderUrl: String?,
) {
    fun toRecipePreview() =
        RecipePreview(
            id = id,
            name = name,
            keywords = keywords?.split(",")?.toSet() ?: emptySet(),
            category = category ?: "",
            imageUrl = imageUrl ?: "",
            createdAt = dateCreated ?: "",
            modifiedAt = dateModified ?: "",
        )
}
