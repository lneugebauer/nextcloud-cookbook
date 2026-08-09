package de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeSyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: String): Flow<RecipeEntity?>

    @Query("SELECT id, dateModified FROM recipes")
    suspend fun getSyncStates(): List<RecipeSyncState>

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
