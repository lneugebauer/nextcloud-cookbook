package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.UNCATEGORIZED_RECIPE
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
    /**
     * The category this recipe belongs to, with recipes lacking one mapped to the
     * [UNCATEGORIZED_RECIPE] pseudo category the server itself uses in `GET /categories`.
     */
    val categoryOrUncategorized: String
        get() = category?.takeIf { it.isNotBlank() } ?: UNCATEGORIZED_RECIPE

    /**
     * This recipe's id, falling back to the [recipeId] that servers before Cookbook v0.10.3
     * send instead, or `null` if neither is usable.
     */
    val idOrNull: String?
        get() = id?.takeIf { it.isNotBlank() } ?: recipeId?.takeIf { it.isNotBlank() }

    @Throws(IllegalStateException::class)
    fun toRecipePreview() =
        RecipePreview(
            id = idOrNull ?: throw IllegalStateException("Both 'id' and 'recipe_id' are null or blank"),
            name = name,
            keywords = keywords?.split(",")?.toSet() ?: emptySet(),
            category = category ?: "",
            imageUrl = imageUrl ?: "",
            createdAt = dateCreated ?: "",
            modifiedAt = dateModified ?: "",
        )
}
