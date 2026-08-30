package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.extractHttpUrl
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeConflictDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.ConflictState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadRecipeViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<DownloadRecipeScreenState>(DownloadRecipeScreenState.Initial())
        val uiState = _uiState.asStateFlow()

        private val _conflict = MutableStateFlow<ConflictState>(ConflictState.None)
        val conflict: StateFlow<ConflictState> = _conflict.asStateFlow()

        init {
            val sharedText: String? = savedStateHandle["sharedText"]
            if (!sharedText.isNullOrBlank()) {
                val url = sharedText.extractHttpUrl()
                _uiState.value = DownloadRecipeScreenState.Initial(url = url ?: sharedText)
                if (url != null && savedStateHandle.get<Boolean>("autoImportTriggered") != true) {
                    savedStateHandle["autoImportTriggered"] = true
                    importRecipe()
                }
            }
        }

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
            dismissConflict()
            viewModelScope.launch {
                val currentState = _uiState.value
                if (currentState is DownloadRecipeScreenState.Initial) {
                    _uiState.update { DownloadRecipeScreenState.Loading(url = currentState.url) }
                    val url = ImportUrlDto(url = currentState.url)
                    val result = recipeRepository.importRecipe(url)
                    when {
                        result is Resource.Success && result.data != null -> {
                            _uiState.update { DownloadRecipeScreenState.Loaded(id = result.data.id) }
                        }
                        else -> {
                            val conflictDto = result.data as? RecipeConflictDto
                            if (conflictDto != null) {
                                handleConflict(name = conflictDto.name, id = conflictDto.id, url = currentState.url)
                            } else {
                                _uiState.update {
                                    DownloadRecipeScreenState.Error(
                                        url = currentState.url,
                                        uiText =
                                            result.message
                                                ?: UiText.StringResource(R.string.error_unknown),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun handleConflict(
            name: String,
            id: String?,
            url: String,
        ) {
            _conflict.value = ConflictState.Active(name = name, conflictingRecipeId = id)
            _uiState.update { DownloadRecipeScreenState.Initial(url = url) }
        }

        fun dismissConflict() {
            _conflict.value = ConflictState.None
        }
    }
