package de.lukasneugebauer.nextcloudcookbook.data

import android.net.Uri
import androidx.core.net.toUri
import de.lukasneugebauer.nextcloudcookbook.BuildConfig

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
        imageUrl = (BuildConfig.NC_BASE_URL + imageUrl).toUri(),
        createdAt = dateCreated,
        modifiedAt = dateModified,
    )
}

data class RecipePreview(
    val id: Int,
    val name: String,
    val keywords: List<String>,
    val imageUrl: Uri,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
)