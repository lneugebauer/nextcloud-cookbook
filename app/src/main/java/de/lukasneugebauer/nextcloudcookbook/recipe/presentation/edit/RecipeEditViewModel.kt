package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.edit

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
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase.GetAllKeywordsUseCase
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.util.ImageCompressionService
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeCreateEditViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel
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
                    when (val result = recipeRepository.updateRecipe(recipeDto)) {
                        is Resource.Error -> {
                            val messageRes = result.message as? UiText.StringResource
                            if (messageRes?.resId == R.string.error_recipe_exists) {
                                val idArg = messageRes.args.getOrNull(0) as? String
                                val nameArg = messageRes.args.getOrNull(1) as? String
                                if (nameArg != null && idArg != null) {
                                    handleConflict(name = nameArg, id = idArg)
                                } else {
                                    val name = (messageRes.args.getOrNull(0) as? String) ?: recipeDto.name
                                    handleConflict(name = name, id = null)
                                }
                            } else {
                                _uiState.update {
                                    RecipeCreateEditState.Error(
                                        result.message ?: UiText.StringResource(R.string.error_unknown),
                                    )
                                }
                            }
                        }

                        is Resource.Success -> _uiState.update { RecipeCreateEditState.Updated(recipeDto.id) }
                    }
                }
            }
        }
    }
