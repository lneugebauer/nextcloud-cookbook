package de.lukasneugebauer.nextcloudcookbook.data.repositories

import androidx.annotation.StringRes
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.api.NextcloudApi
import de.lukasneugebauer.nextcloudcookbook.data.models.category.Category
import de.lukasneugebauer.nextcloudcookbook.data.models.recipe.Recipe
import de.lukasneugebauer.nextcloudcookbook.data.models.recipe.RecipePreview
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val nextcloudApi: NextcloudApi
) {

    suspend fun getCategories(): List<Category> =
        nextcloudApi.getCategories().map { it.toCategory() }

    suspend fun getRecipes(): List<RecipePreview> =
        nextcloudApi.getRecipes().map { it.toRecipePreview() }

    suspend fun getRecipesByCategory(category: String): List<RecipePreview> =
        nextcloudApi.getRecipesByCategory(category).map { it.toRecipePreview() }

    suspend fun getRecipe(id: Int): Recipe = nextcloudApi.getRecipe(id).toRecipe()

    suspend fun getHomeScreenData(): List<HomeScreenData> {
        val data = mutableListOf<HomeScreenData>()

        val recipes = getRecipes()
        val randomRecipeId = recipes.random().id
        val randomRecipe = getRecipe(randomRecipeId)
        data.add(
            HomeScreenData.Single(
                headline = R.string.home_recommendation,
                recipe = randomRecipe
            )
        )

        val categories = getCategories()

        val topCategories = categories.sortedByDescending { it.recipeCount }.take(2)
        topCategories.forEach {
            val recipesByCategory = getRecipesByCategory(it.name)
            data.add(
                HomeScreenData.Row(
                    headline = R.string.home_category,
                    recipes = recipesByCategory
                )
            )
        }

        data.add(
            HomeScreenData.Grid(
                headline = R.string.common_categories,
                categories = categories
            )
        )

        return data
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