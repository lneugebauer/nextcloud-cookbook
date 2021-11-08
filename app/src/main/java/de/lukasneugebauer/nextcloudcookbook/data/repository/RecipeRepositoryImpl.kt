package de.lukasneugebauer.nextcloudcookbook.data.repository

import androidx.annotation.StringRes
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RecipeRepository {

    private val cookbookApi = apiProvider.getNcCookbookApi()

    override suspend fun getCategories(): Resource<List<Category>> {
        if (cookbookApi == null) {
            return Resource.Error(text = "API not initialized.")
        }
        val categories = cookbookApi.getCategories().map { it.toCategory() }
        return Resource.Success(data = categories)
    }

    override suspend fun getRecipes(): Resource<List<RecipePreview>> {
        Logger.d("apiProvider.getCookbookApi(): ${apiProvider}")
        if (cookbookApi == null) {
            return Resource.Error(text = "API not initialized.")
        }
        val recipes = cookbookApi.getRecipes()
        Logger.d("recipes: $recipes")
        return Resource.Success(data = recipes.map { it.toRecipePreview() })
    }

    override suspend fun getRecipesByCategory(category: String): Resource<List<RecipePreview>> {
        if (cookbookApi == null) {
            return Resource.Error(text = "API not initialized.")
        }
        val recipes = cookbookApi.getRecipesByCategory(category).map { it.toRecipePreview() }
        return Resource.Success(data = recipes)
    }

    override suspend fun getRecipe(id: Int): Resource<Recipe> {
        if (cookbookApi == null) {
            return Resource.Error(text = "API not initialized.")
        }
        val recipe = cookbookApi.getRecipe(id).toRecipe()
        return Resource.Success(data = recipe)
    }

    override suspend fun getHomeScreenData(): Resource<List<HomeScreenData>> {
        // TODO: 01.11.21 Move this into use case
        val data = mutableListOf<HomeScreenData>()
        var error = ""

        val recipes = getRecipes()
        if (recipes is Resource.Success && recipes.data != null) {
            val randomRecipeId = recipes.data.random().id
            val randomRecipe = getRecipe(randomRecipeId)
            if (randomRecipe is Resource.Success && randomRecipe.data != null) {
                data.add(
                    HomeScreenData.Single(
                        headline = R.string.home_recommendation,
                        recipe = randomRecipe.data
                    )
                )
            } else {
                error += randomRecipe.text
            }
        } else {
            error += recipes.text
        }

        val categories = getCategories()
        if (categories is Resource.Success && categories.data != null) {
            val topCategories = categories.data.sortedByDescending { it.recipeCount }.take(2)
            topCategories.forEach {
                val recipesByCategory = getRecipesByCategory(it.name)
                if (recipesByCategory is Resource.Success && recipesByCategory.data != null) {
                    data.add(
                        HomeScreenData.Row(
                            headline = R.string.home_category,
                            recipes = recipesByCategory.data
                        )
                    )
                } else {
                    error += recipesByCategory.text
                }
            }

            data.add(
                HomeScreenData.Grid(
                    headline = R.string.common_categories,
                    categories = categories.data
                )
            )
        } else {
            error += categories.text
        }

        return if (error.isNotBlank()) {
            Resource.Error(data = data, text = error)
        } else {
            Resource.Success(data = data)
        }
    }
}

sealed class HomeScreenData {
    abstract val headline: Int

    data class Grid(
        @StringRes override val headline: Int,
        val categories: List<Category>
    ) : HomeScreenData()

    data class Row(
        @StringRes override val headline: Int,
        val recipes: List<RecipePreview>
    ) : HomeScreenData()

    data class Single(
        @StringRes override val headline: Int,
        val recipe: Recipe
    ) : HomeScreenData()
}