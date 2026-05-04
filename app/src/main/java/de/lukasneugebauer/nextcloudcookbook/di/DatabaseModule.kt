package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.category.domain.dao.CategoryDao
import de.lukasneugebauer.nextcloudcookbook.core.data.CookbookDatabase
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipePreviewDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CookbookDatabase =
        Room.databaseBuilder(
            context,
            CookbookDatabase::class.java,
            "cookbook.db",
        )
            .build()

    @Provides
    fun provideRecipePreviewDao(db: CookbookDatabase): RecipePreviewDao = db.recipePreviewDao()

    @Provides
    fun provideRecipeDao(db: CookbookDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideCategoryDao(db: CookbookDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

}