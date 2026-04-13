package de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipePreviewDao {
    @Query("SELECT * FROM recipe_previews")
    fun getAll(): Flow<List<RecipePreviewEntity>>

    @Query("SELECT * FROM recipe_previews WHERE category = :category")
    fun getByCategory(category: String): Flow<List<RecipePreviewEntity>>

    @Upsert
    suspend fun upsertAll(previews: List<RecipePreviewEntity>)

    @Query("DELETE FROM recipe_previews")
    suspend fun deleteAll()
}

