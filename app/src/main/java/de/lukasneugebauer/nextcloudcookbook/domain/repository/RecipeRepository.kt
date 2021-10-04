package de.lukasneugebauer.nextcloudcookbook.domain.repository

import de.lukasneugebauer.nextcloudcookbook.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.data.repository.HomeScreenData

interface RecipeRepository {

    suspend fun getCategories(): List<Category>

    suspend fun getRecipes(): List<RecipePreview>

    suspend fun getRecipesByCategory(category: String): List<RecipePreview>

    suspend fun getRecipe(id: Int): Recipe

    suspend fun getHomeScreenData(): List<HomeScreenData>
}