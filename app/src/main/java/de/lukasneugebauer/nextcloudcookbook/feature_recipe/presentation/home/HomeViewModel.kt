package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeConstants.HOME_SCREEN_CATEGORIES
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val preferencesManager: PreferencesManager,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state: MutableState<HomeScreenState> = mutableStateOf(HomeScreenState())
    val state: State<HomeScreenState> = _state

    init {
        viewModelScope.launch {
            getHomeScreenData()
        }
    }

    @FlowPreview
    private suspend fun getHomeScreenData() {
        // TODO: 01.11.21 Move this into use case
        combine(
            preferencesManager.preferencesFlow.map { it.recipeOfTheDay }.distinctUntilChanged(),
            recipeRepository.getRecipePreviews(),
            categoryRepository.getCategories()
        ) { recipeOfTheDay, recipePreviewsResponse, categoriesResponse ->
            val categories = mutableListOf<Category>()
            val currentDate = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)

            if (recipeOfTheDay.id == 0 || recipeOfTheDay.updatedAt.isBefore(currentDate)) {
                when (recipePreviewsResponse) {
                    is StoreResponse.Loading -> {}
                    is StoreResponse.Data -> {
                        val recipeId = recipePreviewsResponse.value.random().toRecipePreview().id
                        preferencesManager.updateRecipeOfTheDay(
                            RecipeOfTheDay(id = recipeId, updatedAt = LocalDateTime.now())
                        )
                    }
                    is StoreResponse.NoNewData -> {}
                    is StoreResponse.Error.Exception -> {}
                    is StoreResponse.Error.Message -> {}
                }
            }

            when (categoriesResponse) {
                is StoreResponse.Loading -> {}
                is StoreResponse.Data -> {
                    categories.addAll(
                        categoriesResponse.value
                            .sortedByDescending { it.recipeCount }
                            .take(HOME_SCREEN_CATEGORIES)
                            .map { it.toCategory() }
                    )
                }
                is StoreResponse.NoNewData -> {}
                is StoreResponse.Error.Exception -> {}
                is StoreResponse.Error.Message -> {}
            }

            Pair(recipeOfTheDay, categories.toList())
        }.flatMapMerge { (recipeOfTheDay, categories) ->
            val flows = categories.map { recipeRepository.getRecipePreviewsByCategory(it.name) }
            combine(flows) { recipePreviewsByCategoryResponses ->
                Pair(categories, recipePreviewsByCategoryResponses.toList())
            }.zip(recipeRepository.getRecipe(recipeOfTheDay.id)) { (categories, recipePreviewsByCategoryResponses), recipeOfTheDayResponse ->
                Triple(categories, recipePreviewsByCategoryResponses, recipeOfTheDayResponse)
            }
        }.collect { (categories, recipePreviewsByCategoryResponses, recipeOfTheDayResponse) ->
            val data = mutableListOf<HomeScreenData>()

            when (recipeOfTheDayResponse) {
                is StoreResponse.Loading -> {}
                is StoreResponse.Data -> {
                    data.add(
                        HomeScreenData.Single(
                            R.string.home_recommendation,
                            recipeOfTheDayResponse.value.toRecipe()
                        )
                    )
                }
                is StoreResponse.NoNewData -> {}
                is StoreResponse.Error.Exception -> {}
                is StoreResponse.Error.Message -> {}
            }

            recipePreviewsByCategoryResponses.forEachIndexed { index, recipePreviewsByCategoryResponse ->
                Timber.d("index: $index")
                when (recipePreviewsByCategoryResponse) {
                    is StoreResponse.Loading -> {
                        Timber.d("loading")
                    }
                    is StoreResponse.Data -> {
                        Timber.d("data")
                        data.add(
                            HomeScreenData.Row(
                                categories[index].name,
                                recipePreviewsByCategoryResponse.value.map { it.toRecipePreview() }
                            )
                        )
                    }
                    is StoreResponse.NoNewData -> {
                        Timber.d("no new data")
                    }
                    is StoreResponse.Error.Exception -> {
                        Timber.d("error exception")
                    }
                    is StoreResponse.Error.Message -> {
                        Timber.d("error message")
                    }
                }
            }

            _state.value = _state.value.copy(loading = false, data = data)
        }
    }
}

sealed class HomeScreenData {
    data class Row(
        val headline: String,
        val recipes: List<RecipePreview>
    ) : HomeScreenData()

    data class Single(
        @StringRes val headline: Int,
        val recipe: Recipe
    ) : HomeScreenData()
}