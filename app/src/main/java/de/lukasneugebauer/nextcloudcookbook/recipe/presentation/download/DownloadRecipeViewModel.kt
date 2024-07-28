package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadRecipeViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DownloadRecipeScreenState())
        val uiState = _uiState.asStateFlow()

        fun updateUrl(newUrl: String) {
            _uiState.update {
                it.copy(url = newUrl)
            }
        }

        fun importRecipe() {
            viewModelScope.launch {
                val url = ImportUrlDto(url = _uiState.value.url)
                val result = recipeRepository.importRecipe(url)
                when {
                    result is Resource.Success && result.data != null -> {
                        _uiState.update { it.copy(recipeId = result.data.id) }
                    }
                    else -> _uiState.update { it.copy(error = result.message) }
                }
            }
        }
    }
