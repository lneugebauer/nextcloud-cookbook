package de.lukasneugebauer.nextcloudcookbook.data.repository

import androidx.annotation.StringRes
import com.dropbox.android.external.store4.get
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val categoriesStore: CategoriesStore,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore
) : RecipeRepository {

    override suspend fun getCategories(): Resource<List<Category>> {
        val categories = categoriesStore.get(Unit).map { it.toCategory() }
        return Resource.Success(data = categories)
    }

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
                            categoryName = it.name,
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
        val categoryName: String,
        val recipes: List<RecipePreview>
    ) : HomeScreenData()

    data class Single(
        @StringRes override val headline: Int,
        val recipe: Recipe
    ) : HomeScreenData()
}