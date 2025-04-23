package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import kotlin.jvm.Throws

data class RecipePreviewDto(
    @Deprecated(message = "As of Cookbook v0.10.3, this field is deprecated.", replaceWith = ReplaceWith(expression = "id"))
    @SerializedName("recipe_id")
    val recipeId: String?,
    @SerializedName("id")
    val id: String?,
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
    @Throws(IllegalStateException::class)
    fun toRecipePreview() =
        RecipePreview(
            id = if (!id.isNullOrBlank()) id else recipeId ?: throw IllegalStateException("Both 'id' and 'recipe_id' are null or blank"),
            name = name,
            keywords = keywords?.split(",")?.toSet() ?: emptySet(),
            category = category ?: "",
            imageUrl = imageUrl ?: "",
            createdAt = dateCreated ?: "",
            modifiedAt = dateModified ?: "",
        )
}
