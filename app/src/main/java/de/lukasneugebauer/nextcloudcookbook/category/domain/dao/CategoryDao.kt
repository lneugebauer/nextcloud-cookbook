package de.lukasneugebauer.nextcloudcookbook.category.domain.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceCategories(categories: List<CategoryEntity>) {
        deleteAll()
        upsertAll(categories)
    }

}