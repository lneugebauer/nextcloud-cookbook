package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeConflictDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.ifSuccess
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase.GetAllKeywordsUseCase
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.util.ImageCompressionService
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeCreateViewModel
    @Inject
    constructor(
        categoryRepository: CategoryRepository,
        getAllKeywordsUseCase: GetAllKeywordsUseCase,
        imageCompressionService: ImageCompressionService,
        private val recipeRepository: RecipeRepository,
        savedStateHandle: SavedStateHandle,
    ) : RecipeCreateEditViewModel(
            categoryRepository,
            getAllKeywordsUseCase,
            imageCompressionService,
            recipeRepository,
            savedStateHandle,
        ) {
        override fun save() {
            _uiState.value.ifSuccess {
                dismissConflict()
                _uiState.update { RecipeCreateEditState.Loading }
                viewModelScope.launch {
                    val result = recipeRepository.createRecipe(recipeDto)
                    if (result is Resource.Success && result.data != null) {
                        val recipeId = result.data
                        _uiState.update { RecipeCreateEditState.Updated(recipeId) }
                    } else {
                        val conflict = result.data as? RecipeConflictDto
                        if (conflict != null) {
                            handleConflict(name = conflict.name, id = conflict.id)
                        } else {
                            _uiState.update {
                                RecipeCreateEditState.Error(
                                    result.message ?: UiText.StringResource(R.string.error_unknown),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
