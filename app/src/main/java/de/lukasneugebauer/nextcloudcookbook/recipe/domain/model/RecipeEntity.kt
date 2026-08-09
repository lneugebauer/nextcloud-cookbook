package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val json: String,
    /**
     * Mirrors the recipe's `dateModified`, kept out of [json] so the sync can tell which
     * recipes changed without deserialising every cached recipe.
     */
    val dateModified: String?,
)

/**
 * Just enough of a [RecipeEntity] to decide whether the cached copy is still current.
 */
data class RecipeSyncState(
    val id: String,
    val dateModified: String?,
)
