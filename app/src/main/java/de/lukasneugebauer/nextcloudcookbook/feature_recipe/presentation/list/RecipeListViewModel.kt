package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(RecipeListState())
    val state: State<RecipeListState> = _state

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
                    is StoreResponse.Loading -> {}
                    is StoreResponse.Data -> _state.value =
                        RecipeListState(data = recipePreviewsResponse.value.map { it.toRecipePreview() })
                    is StoreResponse.NoNewData -> {}
                    is StoreResponse.Error.Exception -> {}
                    is StoreResponse.Error.Message -> {}
                }
            }
        }
    }
}