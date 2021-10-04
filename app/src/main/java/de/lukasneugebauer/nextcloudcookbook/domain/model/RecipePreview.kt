package de.lukasneugebauer.nextcloudcookbook.domain.model

import android.net.Uri

data class RecipePreview(
    val id: Int,
    val name: String,
    val keywords: List<String>,
    val imageUrl: Uri,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
)