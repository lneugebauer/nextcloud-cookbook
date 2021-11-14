package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(RecipesScreenState())
    val state: State<RecipesScreenState> = _state

    init {
        viewModelScope.launch {
            val categoryName: String? = savedStateHandle["categoryName"]
            val recipePreviewsResult =
                if (categoryName == null) {
                    recipeRepository.getRecipes()
                } else {
                    recipeRepository.getRecipesByCategory(categoryName)
                }
            when (recipePreviewsResult) {
                is Resource.Success -> _state.value =
                    RecipesScreenState(data = recipePreviewsResult.data ?: emptyList())
                is Resource.Error -> TODO("recipes resource error")
            }

        }
    }
}