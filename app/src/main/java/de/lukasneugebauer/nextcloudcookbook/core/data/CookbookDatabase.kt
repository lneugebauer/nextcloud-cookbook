package de.lukasneugebauer.nextcloudcookbook.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.lukasneugebauer.nextcloudcookbook.category.domain.dao.CategoryDao
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.CategoryEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.CategoryRecipePreviewDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipePreviewDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CategoryRecipePreviewEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeEntity
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreviewEntity

@Database(
    version = 2,
    entities = [RecipePreviewEntity::class, CategoryRecipePreviewEntity::class, RecipeEntity::class, CategoryEntity::class],
    exportSchema = true,
)
abstract class CookbookDatabase : RoomDatabase() {
    abstract fun recipePreviewDao(): RecipePreviewDao

    abstract fun categoryRecipePreviewDao(): CategoryRecipePreviewDao

    abstract fun recipeDao(): RecipeDao

    abstract fun categoryDao(): CategoryDao
}
