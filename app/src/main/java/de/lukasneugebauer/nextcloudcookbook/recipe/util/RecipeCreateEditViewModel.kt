package de.lukasneugebauer.nextcloudcookbook.recipe.util

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.ifSuccess
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.DEFAULT_YIELD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class RecipeCreateEditViewModel(
    private val categoryRepository: CategoryRepository,
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected val _uiState = MutableStateFlow<RecipeCreateEditState>(RecipeCreateEditState.Loading)
    val uiState: StateFlow<RecipeCreateEditState> = _uiState

    private var prepTime: DurationComponents = DurationComponents()

    private var cookTime: DurationComponents = DurationComponents()

    private var totalTime: DurationComponents = DurationComponents()

    private var categories: List<Category> = emptyList()
        set(value) {
            field = value
            _uiState.update {
                RecipeCreateEditState.Success(
                    recipe = recipeDto.toRecipe(),
                    prepTime = prepTime,
                    cookTime = cookTime,
                    totalTime = totalTime,
                    categories = categories,
                )
            }
        }

    protected var recipeDto: RecipeDto = emptyRecipeDto()
        set(value) {
            field = value
            val recipe = recipeDto.toRecipe()
            _uiState.update {
                RecipeCreateEditState.Success(
                    recipe = recipe,
                    prepTime = prepTime,
                    cookTime = cookTime,
                    totalTime = totalTime,
                    categories = categories,
                )
            }
        }

    init {
        getCategories()

        val recipeId: Int? = savedStateHandle["recipeId"]
        recipeId?.let {
            getRecipe(it)
        } ?: run {
            _uiState.update {
                RecipeCreateEditState.Success(
                    recipeDto.toRecipe(),
                    prepTime,
                    cookTime,
                    totalTime,
                    categories,
                )
            }
        }
    }

    abstract fun save()

    fun changeName(newName: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(name = newName)
        }
    }

    fun changeDescription(newDescription: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(description = newDescription)
        }
    }

    fun changeUrl(newUrl: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(url = newUrl)
        }
    }

    fun changeImageOrigin(newImageOrigin: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(image = newImageOrigin)
        }
    }

    fun changePrepTime(newPrepTime: DurationComponents) {
        _uiState.value.ifSuccess {
            prepTime = newPrepTime
            recipeDto = recipeDto.copy(prepTime = newPrepTime.toIsoStringOrNull())
        }
    }

    fun changeCookTime(newCookTime: DurationComponents) {
        _uiState.value.ifSuccess {
            cookTime = newCookTime
            recipeDto = recipeDto.copy(cookTime = newCookTime.toIsoStringOrNull())
        }
    }

    fun changeTotalTime(newTotalTime: DurationComponents) {
        _uiState.value.ifSuccess {
            totalTime = newTotalTime
            recipeDto = recipeDto.copy(totalTime = newTotalTime.toIsoStringOrNull())
        }
    }

    fun changeCategory(newCategory: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(recipeCategory = newCategory)
        }
    }

    fun changeYield(newYield: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(recipeYield = newYield.toIntOrNull() ?: DEFAULT_YIELD)
        }
    }

    fun changeIngredient(index: Int, newIngredient: String) {
        _uiState.value.ifSuccess {
            val ingredients = recipeDto.recipeIngredient.toMutableList()
            ingredients[index] = newIngredient
            recipeDto = recipeDto.copy(recipeIngredient = ingredients)
        }
    }

    fun deleteIngredient(index: Int) {
        _uiState.value.ifSuccess {
            val ingredients = recipeDto.recipeIngredient.toMutableList()
            ingredients.removeAt(index)
            recipeDto = recipeDto.copy(recipeIngredient = ingredients)
        }
    }

    fun addIngredient() {
        _uiState.value.ifSuccess {
            val ingredients = recipeDto.recipeIngredient.toMutableList()
            ingredients.add("")
            recipeDto = recipeDto.copy(recipeIngredient = ingredients)
        }
    }

    fun changeTool(index: Int, newTool: String) {
        _uiState.value.ifSuccess {
            val tools = recipeDto.tool.toMutableList()
            tools[index] = newTool
            recipeDto = recipeDto.copy(tool = tools)
        }
    }

    fun deleteTool(index: Int) {
        _uiState.value.ifSuccess {
            val tools = recipeDto.tool.toMutableList()
            tools.removeAt(index)
            recipeDto = recipeDto.copy(tool = tools)
        }
    }

    fun addTool() {
        _uiState.value.ifSuccess {
            val tools = recipeDto.tool.toMutableList()
            tools.add("")
            recipeDto = recipeDto.copy(tool = tools)
        }
    }

    fun changeInstruction(index: Int, newInstruction: String) {
        _uiState.value.ifSuccess {
            val instructions = recipeDto.recipeInstructions.toMutableList()
            instructions[index] = newInstruction
            recipeDto = recipeDto.copy(recipeInstructions = instructions)
        }
    }

    fun deleteInstruction(index: Int) {
        _uiState.value.ifSuccess {
            val instructions = recipeDto.recipeInstructions.toMutableList()
            instructions.removeAt(index)
            recipeDto = recipeDto.copy(recipeInstructions = instructions)
        }
    }

    fun addInstruction() {
        _uiState.value.ifSuccess {
            val instructions = recipeDto.recipeInstructions.toMutableList()
            instructions.add("")
            recipeDto = recipeDto.copy(recipeInstructions = instructions)
        }
    }

    private fun getCategories() {
        categoryRepository.getCategories().onEach { categoriesResponse ->
            when (categoriesResponse) {
                is StoreResponse.Data ->
                    categories = categoriesResponse.value
                        .filter { it.recipeCount > 0 }
                        .filter { it.name != "*" }
                        .map { it.toCategory() }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            recipeDto = recipeRepository.getRecipe(id).also { dto ->
                dto.toRecipe().also {
                    it.prepTime?.toDurationComponents()?.run { prepTime = this }
                    it.cookTime?.toDurationComponents()?.run { cookTime = this }
                    it.totalTime?.toDurationComponents()?.run { totalTime = this }
                }
            }
        }
    }
}
