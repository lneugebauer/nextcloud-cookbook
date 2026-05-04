package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_previews")
data class RecipePreviewEntity(
    @PrimaryKey val id: String,
    val name: String,
    val keywords: String?,
    val category: String?,
    val dateCreated: String?,
    val dateModified: String?,
    val imageUrl: String?,
    val imagePlaceholderUrl: String?,
)