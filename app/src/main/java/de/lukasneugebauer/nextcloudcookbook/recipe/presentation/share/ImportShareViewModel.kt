package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.share

import android.util.Patterns
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportShareViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) : ViewModel() {
        private val _state =
            MutableStateFlow<DownloadRecipeScreenState>(DownloadRecipeScreenState.Initial())
        val state: StateFlow<DownloadRecipeScreenState> = _state.asStateFlow()

        fun importFromSharedText(sharedText: String?) {
            val text = sharedText.orEmpty()
            val url = extractFirstHttpUrl(text)
            if (url == null) {
                _state.update {
                    DownloadRecipeScreenState.Error(
                        url = "",
                        uiText = UiText.StringResource(R.string.error_invalid_url),
                    )
                }
                return
            }

            viewModelScope.launch {
                _state.update { DownloadRecipeScreenState.Loading(url = url) }
                val result = recipeRepository.importRecipe(ImportUrlDto(url))
                when {
                    result is Resource.Success && result.data != null -> {
                        _state.update { DownloadRecipeScreenState.Loaded(id = result.data.id) }
                    }

                    else -> {
                        _state.update {
                            DownloadRecipeScreenState.Error(
                                url = url,
                                uiText =
                                    result.message
                                        ?: UiText.StringResource(R.string.error_unknown),
                            )
                        }
                    }
                }
            }
        }

        private fun extractFirstHttpUrl(text: String): String? {
            // Use Android's WEB_URL pattern but ensure http(s) scheme
            val matcher = Patterns.WEB_URL.matcher(text)
            while (matcher.find()) {
                val candidate = matcher.group()
                if (candidate != null && candidate.startsWith("https://", true)) {
                    return candidate
                }
            }
            return null
        }
    }
