package de.lukasneugebauer.nextcloudcookbook.ui.recipe

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state: MutableState<RecipeDetailScreenState> =
        mutableStateOf(RecipeDetailScreenState.Initial)
    val state: State<RecipeDetailScreenState> = _state

    fun getRecipe(id: Int) {
        viewModelScope.launch {
            when (val recipeResource = recipeRepository.getRecipe(id)) {
                is Resource.Success -> {
                    if (recipeResource.data != null) {
                        _state.value = RecipeDetailScreenState.Loaded(data = recipeResource.data)
                    }
                }
                is Resource.Error -> TODO("recipe resource error")
            }
        }
    }
}

sealed class RecipeDetailScreenState {
    object Initial : RecipeDetailScreenState()
    data class Loaded(val data: Recipe) : RecipeDetailScreenState()
}