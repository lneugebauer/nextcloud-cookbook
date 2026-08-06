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
import de.lukasneugebauer.nextcloudcookbook.recipe.util.ConflictInfo
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

        private val _conflict = MutableStateFlow<ConflictInfo?>(null)
        val conflict = _conflict.asStateFlow()

        fun dismissConflict() {
            _conflict.value = null
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
                            val messageRes = result.message as? UiText.StringResource
                            if (messageRes?.resId == R.string.error_recipe_exists) {
                                val idArg = messageRes.args.getOrNull(0) as? String
                                val nameArg = messageRes.args.getOrNull(1) as? String
                                if (nameArg != null && idArg != null) {
                                    _conflict.value = ConflictInfo(name = nameArg, conflictingRecipeId = idArg)
                                } else {
                                    val name = (messageRes.args.getOrNull(0) as? String) ?: currentState.url
                                    _conflict.value = ConflictInfo(name = name, conflictingRecipeId = null)
                                }
                                _uiState.update { DownloadRecipeScreenState.Initial(url = currentState.url) }
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
    }
