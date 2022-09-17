package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeListState())
    val state: StateFlow<RecipeListState> = _state

    init {
        viewModelScope.launch {
            val categoryName: String? = savedStateHandle["categoryName"]
            val recipePreviewsFlow =
                if (categoryName == null) {
                    recipeRepository.getRecipePreviews()
                } else {
                    recipeRepository.getRecipePreviewsByCategory(categoryName)
                }

            recipePreviewsFlow.collect { recipePreviewsResponse ->
                when (recipePreviewsResponse) {
                    is StoreResponse.Loading -> _state.value = _state.value.copy(loading = true)
                    is StoreResponse.Data -> _state.value = _state.value.copy(
                        loading = false,
                        data = recipePreviewsResponse.value.map { it.toRecipePreview() }
                    )
                    is StoreResponse.NoNewData -> _state.value = _state.value.copy(loading = true)
                    is StoreResponse.Error.Exception -> {}
                    is StoreResponse.Error.Message -> {}
                }
            }
        }
    }
}
