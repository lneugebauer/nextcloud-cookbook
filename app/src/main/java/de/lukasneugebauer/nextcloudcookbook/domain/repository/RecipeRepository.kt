package de.lukasneugebauer.nextcloudcookbook.domain.repository

import de.lukasneugebauer.nextcloudcookbook.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.data.repository.HomeScreenData
import de.lukasneugebauer.nextcloudcookbook.utils.Resource

interface RecipeRepository {

    suspend fun getCategories(): Resource<List<Category>>

    suspend fun getRecipes(): Resource<List<RecipePreview>>

    suspend fun getRecipesByCategory(categoryName: String): Resource<List<RecipePreview>>

    suspend fun getRecipe(id: Int): Resource<Recipe>

    suspend fun getHomeScreenData(): Resource<List<HomeScreenData>>
}