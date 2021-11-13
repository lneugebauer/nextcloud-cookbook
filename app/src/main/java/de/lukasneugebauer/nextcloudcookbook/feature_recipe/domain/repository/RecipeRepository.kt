package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource

interface RecipeRepository {

    suspend fun getRecipes(): Resource<List<RecipePreview>>

    suspend fun getRecipesByCategory(categoryName: String): Resource<List<RecipePreview>>

    suspend fun getRecipe(id: Int): Resource<Recipe>
}