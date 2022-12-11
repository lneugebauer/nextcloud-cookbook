package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeListScreenState>(RecipeListScreenState.Initial)
    val state = _uiState.asStateFlow()

    private val categoryName: String?

    init {
        categoryName = savedStateHandle["categoryName"]
        getRecipePreviews()
    }

    private fun getRecipePreviews() {
        val recipePreviewsFlow = if (categoryName == null) {
            recipeRepository.getRecipePreviews()
        } else {
            recipeRepository.getRecipePreviewsByCategory(categoryName)
        }

        recipePreviewsFlow.onEach { recipePreviewsResponse ->
            when (recipePreviewsResponse) {
                is StoreResponse.Loading -> _uiState.update { RecipeListScreenState.Initial }
                is StoreResponse.Data -> _uiState.update {
                    RecipeListScreenState.Loaded(
                        data = recipePreviewsResponse.value.map { it.toRecipePreview() }
                    )
                }
                is StoreResponse.NoNewData -> Unit
                is StoreResponse.Error -> {
                    val message = recipePreviewsResponse.errorMessageOrNull()
                        ?.let { UiText.DynamicString(it) }
                        ?: run { UiText.StringResource(R.string.error_unknown) }
                    _uiState.update { RecipeListScreenState.Error(message) }
                }
            }
        }.launchIn(viewModelScope)
    }
}
