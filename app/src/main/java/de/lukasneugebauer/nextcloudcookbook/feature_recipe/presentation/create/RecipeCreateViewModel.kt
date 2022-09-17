package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeCreateEditState
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
        if (_uiState.value is RecipeCreateEditState.Success) {
            _uiState.update { RecipeCreateEditState.Loading }
            viewModelScope.launch {
                val result = recipeRepository.createRecipe(recipe)
                _uiState.update {
                    if (result is Resource.Success && result.data != null) {
                        val recipeId = result.data
                        RecipeCreateEditState.Updated(recipeId)
                    } else {
                        RecipeCreateEditState.Error(
                            result.message ?: UiText.StringResource(R.string.error_unknown)
                        )
                    }
                }
            }
        }
    }
}