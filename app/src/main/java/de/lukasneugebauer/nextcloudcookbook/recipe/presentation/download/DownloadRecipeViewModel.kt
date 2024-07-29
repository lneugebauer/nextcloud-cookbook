package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
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
        private val _uiState = MutableStateFlow<DownloadRecipeScreenState>(DownloadRecipeScreenState.Initial())
        val uiState = _uiState.asStateFlow()

        fun updateUrl(newUrl: String) {
            _uiState.update {
                when (it) {
                    is DownloadRecipeScreenState.Initial -> it.copy(url = newUrl)
                    is DownloadRecipeScreenState.Error -> DownloadRecipeScreenState.Initial(url = newUrl)
                    else -> it
                }
            }
        }

        fun importRecipe() {
            viewModelScope.launch {
                val currentState = _uiState.value
                if (currentState is DownloadRecipeScreenState.Initial) {
                    val url = ImportUrlDto(url = currentState.url)
                    val result = recipeRepository.importRecipe(url)
                    when {
                        result is Resource.Success && result.data != null -> {
                            _uiState.update { DownloadRecipeScreenState.Loaded(id = result.data.id) }
                        }
                        else ->
                            _uiState.update {
                                DownloadRecipeScreenState.Error(
                                    url = currentState.url,
                                    uiText = result.message ?: UiText.StringResource(R.string.error_unknown),
                                )
                            }
                    }
                }
            }
        }
    }
