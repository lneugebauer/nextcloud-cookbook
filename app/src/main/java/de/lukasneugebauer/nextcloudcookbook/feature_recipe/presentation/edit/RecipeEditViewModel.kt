package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : RecipeCreateEditViewModel(recipeRepository, savedStateHandle) {

    override fun save() {
        if (_uiState.value is RecipeCreateEditState.Success) {
            _uiState.update { RecipeCreateEditState.Loading }
            viewModelScope.launch {
                _uiState.update {
                    when (val result = recipeRepository.updateRecipe(recipe)) {
                        is Resource.Error -> RecipeCreateEditState.Error(
                            result.text ?: "Unknown error."
                        )
                        is Resource.Success -> RecipeCreateEditState.Updated(recipe.id)
                    }
                }
            }
        }
    }
}
