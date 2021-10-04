package de.lukasneugebauer.nextcloudcookbook.data.repository

import androidx.annotation.StringRes
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val ncCookbookApi: NcCookbookApi
) : RecipeRepository {

    override suspend fun getCategories(): List<Category> =
        ncCookbookApi.getCategories().map { it.toCategory() }

    override suspend fun getRecipes(): List<RecipePreview> =
        ncCookbookApi.getRecipes().map { it.toRecipePreview() }

    override suspend fun getRecipesByCategory(category: String): List<RecipePreview> =
        ncCookbookApi.getRecipesByCategory(category).map { it.toRecipePreview() }

    override suspend fun getRecipe(id: Int): Recipe = ncCookbookApi.getRecipe(id).toRecipe()

    override suspend fun getHomeScreenData(): List<HomeScreenData> {
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