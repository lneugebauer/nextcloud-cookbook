package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeEditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeEditState>(RecipeEditState.Loading)
    val uiState: StateFlow<RecipeEditState> = _state

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        recipeId?.let {
            getRecipe(it)
        } ?: run {
            _state.update { RecipeEditState.Error }
        }
    }

    fun changeName(name: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update { RecipeEditState.Success(recipe = currentState.recipe.copy(name = name)) }
        }
    }

    fun changeDescription(description: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update { RecipeEditState.Success(recipe = currentState.recipe.copy(description = description)) }
        }
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(id).toRecipe()
            _state.update { RecipeEditState.Success(recipe) }
        }
    }
}
