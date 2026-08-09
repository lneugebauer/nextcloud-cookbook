package de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeSyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: String): Flow<RecipeEntity?>

    @Query("SELECT id, syncedDateModified FROM recipes")
    suspend fun getSyncStates(): List<RecipeSyncState>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(recipe: RecipeEntity): Long

    @Query("UPDATE recipes SET json = :json WHERE id = :id")
    suspend fun updateJson(
        id: String,
        json: String,
    )

    @Query("UPDATE recipes SET syncedDateModified = :dateModified WHERE id = :id")
    suspend fun markSynced(
        id: String,
        dateModified: String?,
    )

    /**
     * Stores the recipe body, leaving [RecipeEntity.syncedDateModified] untouched on an existing
     * row. Fetching a recipe outside the sync — opening it, say — must not discard the marker,
     * or the next sync would fetch it all over again.
     */
    @Transaction
    suspend fun upsertJson(
        id: String,
        json: String,
    ) {
        val insertedRowId = insertIfAbsent(RecipeEntity(id = id, json = json, syncedDateModified = null))
        if (insertedRowId == -1L) {
            updateJson(id = id, json = json)
        }
    }

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
