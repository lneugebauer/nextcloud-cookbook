package de.lukasneugebauer.nextcloudcookbook.feature_recipe.util

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeEditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class RecipeCreateEditViewModel(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val _uiState = MutableStateFlow<RecipeEditState>(RecipeEditState.Loading)
    val uiState: StateFlow<RecipeEditState> = _uiState

    protected lateinit var recipe: RecipeDto

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        if (recipeId == null) {
            initWithEmptyRecipe()
        } else {
            getRecipe(recipeId)
        }
    }

    abstract fun save()

    fun changeName(newName: String) {
        if (_uiState.value is RecipeEditState.Success) {
            recipe = recipe.copy(name = newName)
            updateRecipe()
        }
    }

    fun changeDescription(newDescription: String) {
        if (_uiState.value is RecipeEditState.Success) {
            recipe = recipe.copy(description = newDescription)
            updateRecipe()
        }
    }

    fun changeUrl(newUrl: String) {
        if (_uiState.value is RecipeEditState.Success) {
            recipe = recipe.copy(url = newUrl)
            updateRecipe()
        }
    }

    fun changeYield(newYield: String) {
        if (_uiState.value is RecipeEditState.Success) {
            // TODO: Check toInt actually works or don't apply the update
            recipe = recipe.copy(recipeYield = newYield.toInt())
            updateRecipe()
        }
    }

    fun changeIngredient(index: Int, newIngredient: String) {
        if (_uiState.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients[index] = newIngredient
            recipe = recipe.copy(recipeIngredient = ingredients)
            updateRecipe()
        }
    }

    fun deleteIngredient(index: Int) {
        if (_uiState.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.removeAt(index)
            recipe = recipe.copy(recipeIngredient = ingredients)
            updateRecipe()
        }
    }

    fun addIngredient() {
        if (_uiState.value is RecipeEditState.Success) {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.add("")
            recipe = recipe.copy(recipeIngredient = ingredients)
            updateRecipe()
        }
    }

    fun changeTool(index: Int, newTool: String) {
        if (_uiState.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools[index] = newTool
            recipe = recipe.copy(tool = tools)
            updateRecipe()
        }
    }

    fun deleteTool(index: Int) {
        if (_uiState.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools.removeAt(index)
            recipe = recipe.copy(tool = tools)
            updateRecipe()
        }
    }

    fun addTool() {
        if (_uiState.value is RecipeEditState.Success) {
            val tools = recipe.tool.toMutableList()
            tools.add("")
            recipe = recipe.copy(tool = tools)
            updateRecipe()
        }
    }

    fun changeInstruction(index: Int, newInstruction: String) {
        if (_uiState.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions[index] = newInstruction
            recipe = recipe.copy(recipeInstructions = instructions)
            updateRecipe()
        }
    }

    fun deleteInstruction(index: Int) {
        if (_uiState.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.removeAt(index)
            recipe = recipe.copy(recipeInstructions = instructions)
            updateRecipe()
        }
    }

    fun addInstruction() {
        if (_uiState.value is RecipeEditState.Success) {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.add("")
            recipe = recipe.copy(recipeInstructions = instructions)
            updateRecipe()
        }
    }

    private fun initWithEmptyRecipe() {
        recipe = emptyRecipeDto()
        updateRecipe()
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            recipe = recipeRepository.getRecipe(id)
            updateRecipe()
        }
    }

    private fun updateRecipe() {
        _uiState.update { RecipeEditState.Success(recipe.toRecipe()) }
    }
}