package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeEditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow<RecipeEditState>(RecipeEditState.Loading)
    val uiState: StateFlow<RecipeEditState> = _state

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        recipeId?.let {
            getRecipe(it)
        } ?: run {
            _state.update { RecipeEditState.Error }
        }
    }

    fun changeName(newName: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(name = newName))
            }
        }
    }

    fun changeDescription(newDescription: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(description = newDescription))
            }
        }
    }

    fun changeUrl(newUrl: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(url = newUrl))
            }
        }
    }

    fun changeYield(newYield: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(yield = newYield.toInt()))
            }
        }
    }

    fun changeIngredient(index: Int, newIngredient: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val ingredients = currentState.recipe.ingredients.toMutableList()
            ingredients[index] = newIngredient
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(ingredients = ingredients))
            }
        }
    }

    fun deleteIngredient(index: Int) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val ingredients = currentState.recipe.ingredients.toMutableList()
            ingredients.removeAt(index)
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(ingredients = ingredients))
            }
        }
    }

    fun addIngredient() {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val ingredients = currentState.recipe.ingredients.toMutableList()
            ingredients.add("")
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(ingredients = ingredients))
            }
        }
    }

    fun changeInstruction(index: Int, newInstruction: String) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val instructions = currentState.recipe.instructions.toMutableList()
            instructions[index] = newInstruction
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(instructions = instructions))
            }
        }
    }

    fun deleteInstruction(index: Int) {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val instructions = currentState.recipe.instructions.toMutableList()
            instructions.removeAt(index)
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(instructions = instructions))
            }
        }
    }

    fun addInstruction() {
        val currentState = _state.value
        if (currentState is RecipeEditState.Success) {
            val instructions = currentState.recipe.instructions.toMutableList()
            instructions.add("")
            _state.update {
                RecipeEditState.Success(currentState.recipe.copy(instructions = instructions))
            }
        }
    }

    fun save() {
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(id).toRecipe()
            _state.update { RecipeEditState.Success(recipe) }
        }
    }
}
