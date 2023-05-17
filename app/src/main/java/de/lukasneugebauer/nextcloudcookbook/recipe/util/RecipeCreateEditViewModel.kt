package de.lukasneugebauer.nextcloudcookbook.recipe.util

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.ifSuccess
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.DEFAULT_YIELD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class RecipeCreateEditViewModel(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected val _uiState = MutableStateFlow<RecipeCreateEditState>(RecipeCreateEditState.Loading)
    val uiState: StateFlow<RecipeCreateEditState> = _uiState

    protected var recipe: RecipeDto = emptyRecipeDto()
        set(value) {
            field = value
            _uiState.update { RecipeCreateEditState.Success(recipe.toRecipe()) }
        }

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        recipeId?.let {
            getRecipe(it)
        } ?: run {
            _uiState.update { RecipeCreateEditState.Success(recipe.toRecipe()) }
        }
    }

    abstract fun save()

    fun changeName(newName: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(name = newName)
        }
    }

    fun changeDescription(newDescription: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(description = newDescription)
        }
    }

    fun changeUrl(newUrl: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(url = newUrl)
        }
    }

    fun changeImageOrigin(newImageOrigin: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(image = newImageOrigin)
        }
    }

    fun changePrepTime(newPrepTime: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(prepTime = newPrepTime)
        }
    }

    fun changeCookTime(newCookTime: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(cookTime = newCookTime)
        }
    }

    fun changeTotalTime(newTotalTime: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(totalTime = newTotalTime)
        }
    }

    fun changeYield(newYield: String) {
        _uiState.value.ifSuccess {
            recipe = recipe.copy(recipeYield = newYield.toIntOrNull() ?: DEFAULT_YIELD)
        }
    }

    fun changeIngredient(index: Int, newIngredient: String) {
        _uiState.value.ifSuccess {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients[index] = newIngredient
            recipe = recipe.copy(recipeIngredient = ingredients)
        }
    }

    fun deleteIngredient(index: Int) {
        _uiState.value.ifSuccess {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.removeAt(index)
            recipe = recipe.copy(recipeIngredient = ingredients)
        }
    }

    fun addIngredient() {
        _uiState.value.ifSuccess {
            val ingredients = recipe.recipeIngredient.toMutableList()
            ingredients.add("")
            recipe = recipe.copy(recipeIngredient = ingredients)
        }
    }

    fun changeTool(index: Int, newTool: String) {
        _uiState.value.ifSuccess {
            val tools = recipe.tool.toMutableList()
            tools[index] = newTool
            recipe = recipe.copy(tool = tools)
        }
    }

    fun deleteTool(index: Int) {
        _uiState.value.ifSuccess {
            val tools = recipe.tool.toMutableList()
            tools.removeAt(index)
            recipe = recipe.copy(tool = tools)
        }
    }

    fun addTool() {
        _uiState.value.ifSuccess {
            val tools = recipe.tool.toMutableList()
            tools.add("")
            recipe = recipe.copy(tool = tools)
        }
    }

    fun changeInstruction(index: Int, newInstruction: String) {
        _uiState.value.ifSuccess {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions[index] = newInstruction
            recipe = recipe.copy(recipeInstructions = instructions)
        }
    }

    fun deleteInstruction(index: Int) {
        _uiState.value.ifSuccess {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.removeAt(index)
            recipe = recipe.copy(recipeInstructions = instructions)
        }
    }

    fun addInstruction() {
        _uiState.value.ifSuccess {
            val instructions = recipe.recipeInstructions.toMutableList()
            instructions.add("")
            recipe = recipe.copy(recipeInstructions = instructions)
        }
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            recipe = recipeRepository.getRecipe(id)
        }
    }
}
