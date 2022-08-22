package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeEditState
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : RecipeCreateEditViewModel(recipeRepository, savedStateHandle) {

    override fun save() {
        if (_uiState.value is RecipeEditState.Success) {
            _uiState.update { RecipeEditState.Loading }
            viewModelScope.launch {
                when (val result = recipeRepository.storeRecipe(recipe)) {
                    is Resource.Error -> _uiState.update {
                        RecipeEditState.Error(result.text ?: "Unknown error.")
                    }
                    is Resource.Success -> _uiState.update { RecipeEditState.Updated }
                }
            }
        }
    }
}