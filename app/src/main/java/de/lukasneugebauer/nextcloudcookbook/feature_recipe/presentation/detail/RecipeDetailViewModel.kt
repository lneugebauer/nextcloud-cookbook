package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeDetailState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = mutableStateOf(RecipeDetailState())
    val state: State<RecipeDetailState> = _state

    fun getRecipe(id: Int) {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            recipeRepository.getRecipe(id).collect { recipeResponse ->
                when (recipeResponse) {
                    is StoreResponse.Loading -> _state.value = _state.value.copy(loading = true)
                    is StoreResponse.Data -> _state.value =
                        _state.value.copy(data = recipeResponse.value.toRecipe(), loading = false)
                    is StoreResponse.NoNewData -> {}
                    is StoreResponse.Error.Exception -> {}
                    is StoreResponse.Error.Message -> _state.value =
                        _state.value.copy(error = recipeResponse.message, loading = false)
                }
            }
        }
    }

    fun getShareText(): String {
        val recipe = _state.value.data ?: return ""

        var textToShare = "Recipe: ${recipe.name}\n\n"
        if (recipe.description.isNotBlank()) {
            textToShare += "${recipe.description}\n\n"
        }

        if (recipe.ingredients.isNotEmpty()) {
            textToShare += "Ingredients\n"
            recipe.ingredients.forEachIndexed { index, ingredient ->
                textToShare += "- $ingredient\n"
                if (recipe.ingredients.size - 1 == index) textToShare += "\n"
            }
        }

        if (recipe.tools.isNotEmpty()) {
            textToShare += "Tools\n"
            recipe.tools.forEachIndexed { index, tool ->
                textToShare += "- $tool\n"
                if (recipe.tools.size - 1 == index) textToShare += "\n"
            }
        }

        if (recipe.instructions.isNotEmpty()) {
            textToShare += "Instructions\n"
            recipe.instructions.forEachIndexed { index, instruction ->
                textToShare += "${index + 1}.) $instruction"
                if (recipe.tools.size - 1 != index) textToShare += "\n\n"
            }
        }

        return textToShare
    }
}