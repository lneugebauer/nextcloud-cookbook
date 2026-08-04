package de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CategoryRecipePreviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryRecipePreviewDao {
    @Query("SELECT * FROM category_recipe_previews WHERE category = :category")
    fun getByCategory(category: String): Flow<List<CategoryRecipePreviewEntity>>

    @Upsert
    suspend fun upsertAll(previews: List<CategoryRecipePreviewEntity>)

    @Query("DELETE FROM category_recipe_previews WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("DELETE FROM category_recipe_previews")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceByCategory(
        category: String,
        recipes: List<CategoryRecipePreviewEntity>,
    ) {
        deleteByCategory(category)
        upsertAll(recipes)
    }
}
