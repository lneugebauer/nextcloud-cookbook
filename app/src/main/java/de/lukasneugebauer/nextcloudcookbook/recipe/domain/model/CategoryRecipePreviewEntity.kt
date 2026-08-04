package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import androidx.room.Entity

@Entity(tableName = "category_recipe_previews", primaryKeys = ["id", "category"])
data class CategoryRecipePreviewEntity(
    val id: String,
    val name: String,
    val keywords: String?,
    val category: String,
    val dateCreated: String?,
    val dateModified: String?,
    val imageUrl: String?,
    val imagePlaceholderUrl: String?,
)
