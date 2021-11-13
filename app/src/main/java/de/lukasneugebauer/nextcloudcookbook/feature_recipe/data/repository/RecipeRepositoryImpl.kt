package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.repository

import com.dropbox.android.external.store4.get
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore
) : RecipeRepository {

    override suspend fun getRecipes(): Resource<List<RecipePreview>> {
        return withContext(Dispatchers.IO) {
            val recipePreviews = recipePreviewsStore.get(Unit).map { it.toRecipePreview() }
            Resource.Success(data = recipePreviews)
        }
    }

    override suspend fun getRecipesByCategory(categoryName: String): Resource<List<RecipePreview>> {
        return withContext(Dispatchers.IO) {
            val recipePreviews = recipePreviewsByCategoryStore.get(categoryName)
                .map { it.toRecipePreview() }
            Resource.Success(data = recipePreviews)
        }
    }

    override suspend fun getRecipe(id: Int): Resource<Recipe> {
        return withContext(Dispatchers.IO) {
            val recipe = recipeStore.get(key = id).toRecipe()
            Resource.Success(data = recipe)
        }
    }
}