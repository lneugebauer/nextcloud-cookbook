package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val json: String,
    /**
     * The `dateModified` the preview list reported when the sync last fetched this recipe.
     *
     * Deliberately not the recipe's own `dateModified`: the sync checks this against
     * `GET /recipes`, and the two endpoints do not report the field identically, so comparing
     * across them marks every recipe as changed. Storing what we compare against is what makes
     * the check hold.
     */
    val syncedDateModified: String?,
)

/**
 * Just enough of a [RecipeEntity] to decide whether the cached copy is still current.
 */
data class RecipeSyncState(
    val id: String,
    val syncedDateModified: String?,
)
