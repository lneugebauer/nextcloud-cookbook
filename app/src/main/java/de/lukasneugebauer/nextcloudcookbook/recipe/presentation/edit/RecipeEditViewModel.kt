package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : RecipeCreateEditViewModel(recipeRepository, savedStateHandle) {

    override fun save() {
        if (_uiState.value is RecipeCreateEditState.Success) {
            _uiState.update { RecipeCreateEditState.Loading }
            viewModelScope.launch {
                _uiState.update {
                    when (val result = recipeRepository.updateRecipe(recipe)) {
                        is Resource.Error -> RecipeCreateEditState.Error(
                            result.message ?: UiText.StringResource(R.string.error_unknown),
                        )
                        is Resource.Success -> RecipeCreateEditState.Updated(recipe.id)
                    }
                }
            }
        }
    }
}
