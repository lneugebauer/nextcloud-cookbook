package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.ifSuccess
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : RecipeCreateEditViewModel(categoryRepository, recipeRepository, savedStateHandle) {

    override fun save() {
        _uiState.value.ifSuccess {
            _uiState.update { RecipeCreateEditState.Loading }
            viewModelScope.launch {
                val result = recipeRepository.createRecipe(recipe)
                _uiState.update {
                    if (result is Resource.Success && result.data != null) {
                        val recipeId = result.data
                        RecipeCreateEditState.Updated(recipeId)
                    } else {
                        RecipeCreateEditState.Error(
                            result.message ?: UiText.StringResource(R.string.error_unknown),
                        )
                    }
                }
            }
        }
    }
}
