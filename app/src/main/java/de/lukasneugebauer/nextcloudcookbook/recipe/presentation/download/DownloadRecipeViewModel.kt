package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DownloadRecipeViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(DownloadRecipeScreenState())
        val uiState = _uiState.asStateFlow()

        fun updateUrl(newUrl: String) {
            _uiState.update {
                it.copy(url = newUrl)
            }
        }
    }
