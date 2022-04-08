package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipeDto
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

    private lateinit var recipe: RecipeDto

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        recipeId?.let {
            getRecipe(it)
        } ?: run {
            _state.update { RecipeEditState.Error("Recipe ID not supplied.") }
        }
    }

    fun changeName(newName: String) {
        if (_state.value is RecipeEditState.Success) {
            recipe = recipe.copy(name = newName)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeDescription(newDescription: String) {
        if (_state.value is RecipeEditState.Success) {
            recipe = recipe.copy(description = newDescription)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeUrl(newUrl: String) {
        if (_state.value is RecipeEditState.Success) {
            recipe = recipe.copy(url = newUrl)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeYield(newYield: String) {
        if (_state.value is RecipeEditState.Success) {
            recipe = recipe.copy(recipeYield = newYield.toInt())
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeIngredient(index: Int, newIngredient: String) {
        if (_state.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients[index] = newIngredient
            recipe = recipe.copy(recipeIngredient = ingredients)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun deleteIngredient(index: Int) {
        if (_state.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.removeAt(index)
            recipe = recipe.copy(recipeIngredient = ingredients)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun addIngredient() {
        if (_state.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.add("")
            recipe = recipe.copy(recipeIngredient = ingredients)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeTool(index: Int, newTool: String) {
        if (_state.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools[index] = newTool
            recipe = recipe.copy(tool = tools)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun deleteTool(index: Int) {
        if (_state.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools.removeAt(index)
            recipe = recipe.copy(tool = tools)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun addTool() {
        if (_state.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools.add("")
            recipe = recipe.copy(tool = tools)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun changeInstruction(index: Int, newInstruction: String) {
        if (_state.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions[index] = newInstruction
            recipe = recipe.copy(recipeInstructions = instructions)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun deleteInstruction(index: Int) {
        if (_state.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.removeAt(index)
            recipe = recipe.copy(recipeInstructions = instructions)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun addInstruction() {
        if (_state.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.add("")
            recipe = recipe.copy(recipeInstructions = instructions)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }

    fun save() {
        if (_state.value is RecipeEditState.Success) {
            _state.update { RecipeEditState.Loading }
            viewModelScope.launch {
                when (val result = recipeRepository.updateRecipe(recipe)) {
                    is Resource.Error -> _state.update {
                        RecipeEditState.Error(result.text ?: "Unknown error.")
                    }
                    is Resource.Success -> _state.update { RecipeEditState.Updated }
                }
            }
        }
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            recipe = recipeRepository.getRecipe(id)
            _state.update { RecipeEditState.Success(recipe.toRecipe()) }
        }
    }
}
