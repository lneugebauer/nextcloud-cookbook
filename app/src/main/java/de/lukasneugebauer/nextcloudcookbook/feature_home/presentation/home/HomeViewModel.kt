package de.lukasneugebauer.nextcloudcookbook.feature_home.presentation.home

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Logger
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val TAG = this.javaClass.name

    private val _state: MutableState<HomeScreenState> =
        mutableStateOf(HomeScreenState(data = emptyList()))
    val state: State<HomeScreenState> = _state

    init {
        viewModelScope.launch {
            when (val homeScreenDataResource = getHomeScreenData()) {
                is Resource.Success -> {
                    _state.value = HomeScreenState(
                        data = homeScreenDataResource.data ?: emptyList()
                    )
                }
                is Resource.Error -> Logger.e("${homeScreenDataResource.text}", TAG)
            }
        }
    }

    private suspend fun getHomeScreenData(): Resource<List<HomeScreenData>> {
        // TODO: 01.11.21 Move this into use case
        val data = mutableListOf<HomeScreenData>()
        var error = ""

        val recipes = recipeRepository.getRecipes()
        if (recipes is Resource.Success && recipes.data != null) {
            val randomRecipeId = recipes.data.random().id
            val randomRecipe = recipeRepository.getRecipe(randomRecipeId)
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

        val categories = categoryRepository.getCategories()
        if (categories is Resource.Success && categories.data != null) {
            val topCategories = categories.data.sortedByDescending { it.recipeCount }.take(2)
            topCategories.forEach {
                val recipesByCategory = recipeRepository.getRecipesByCategory(it.name)
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