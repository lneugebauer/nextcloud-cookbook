package de.lukasneugebauer.nextcloudcookbook.ui.recipes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.data.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.data.RecipeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state: MutableState<RecipesScreenState> =
        mutableStateOf(RecipesScreenState(data = emptyList()))
    val state: State<RecipesScreenState> = _state

    init {
        viewModelScope.launch {
            _state.value = RecipesScreenState(data = recipeRepository.getRecipes())
        }
    }
}

data class RecipesScreenState(
    val data: List<RecipePreview>
)