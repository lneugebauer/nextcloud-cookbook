package de.lukasneugebauer.nextcloudcookbook.settings.presentation.imageuploadfolder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeImageUploadFolderViewModel
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow("")
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val folder = preferencesManager.preferencesFlow.first().recipeImageUploadFolder
                _uiState.update { folder }
            }
        }

        fun updateFolder(newFolder: String) {
            _uiState.update { newFolder }
        }

        fun reset() {
            _uiState.update { Constants.DEFAULT_RECIPE_IMAGE_UPLOAD_FOLDER }
        }

        fun save() {
            val folder = _uiState.value.trim()
            if (folder.isEmpty()) return
            viewModelScope.launch {
                preferencesManager.updateRecipeImageUploadFolder(folder)
            }
        }
    }
