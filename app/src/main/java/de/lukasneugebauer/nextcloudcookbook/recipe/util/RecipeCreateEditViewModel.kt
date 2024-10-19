package de.lukasneugebauer.nextcloudcookbook.recipe.util

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.NutritionDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.ifSuccess
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase.GetAllKeywordsUseCase
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.DEFAULT_YIELD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IndexOutOfBoundsException
import java.util.Collections

abstract class RecipeCreateEditViewModel(
    private val categoryRepository: CategoryRepository,
    private val getAllKeywordsUseCase: GetAllKeywordsUseCase,
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    @Suppress("PropertyName")
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
                    keywords = keywords,
                )
            }
        }

    protected var keywords: Set<String> = emptySet()
        set(value) {
            field = value
            _uiState.update {
                RecipeCreateEditState.Success(
                    recipe = recipeDto.toRecipe(),
                    prepTime = prepTime,
                    cookTime = cookTime,
                    totalTime = totalTime,
                    categories = categories,
                    keywords = keywords,
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
                    keywords = keywords,
                )
            }
        }

    init {
        getCategories()
        getKeywords()

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

    fun changeKeywords(newKeywords: Set<String>) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(keywords = newKeywords.joinToString())
        }
    }

    fun changeYield(newYield: String) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(recipeYield = newYield.toIntOrNull() ?: DEFAULT_YIELD)
        }
    }

    fun changeIngredient(
        index: Int,
        newIngredient: String,
    ) {
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

    fun swapIngredient(
        fromIndex: Int,
        toIndex: Int,
    ) {
        _uiState.value.ifSuccess {
            val ingredients = recipeDto.recipeIngredient.toMutableList()
            try {
                Collections.swap(ingredients, fromIndex, toIndex)
                recipeDto = recipeDto.copy(recipeIngredient = ingredients)
            } catch (e: IndexOutOfBoundsException) {
                Timber.e(e.stackTraceToString())
            }
        }
    }

    fun changeCalories(newCalories: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(calories = newCalories)
                ?: NutritionDto(calories = newCalories)
        changeNutrition(newNutrition)
    }

    fun changeCarbohydrateContent(newCarbohydrateContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(carbohydrateContent = newCarbohydrateContent)
                ?: NutritionDto(carbohydrateContent = newCarbohydrateContent)
        changeNutrition(newNutrition)
    }

    fun changeCholesterolContent(newCholesterolContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(cholesterolContent = newCholesterolContent)
                ?: NutritionDto(cholesterolContent = newCholesterolContent)
        changeNutrition(newNutrition)
    }

    fun changeFatContent(newFatContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(fatContent = newFatContent)
                ?: NutritionDto(fatContent = newFatContent)
        changeNutrition(newNutrition)
    }

    fun changeFiberContent(newFiberContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(fiberContent = newFiberContent)
                ?: NutritionDto(fiberContent = newFiberContent)
        changeNutrition(newNutrition)
    }

    fun changeProteinContent(newProteinContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(proteinContent = newProteinContent)
                ?: NutritionDto(proteinContent = newProteinContent)
        changeNutrition(newNutrition)
    }

    fun changeSaturatedFatContent(newSaturatedFatContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(saturatedFatContent = newSaturatedFatContent)
                ?: NutritionDto(saturatedFatContent = newSaturatedFatContent)
        changeNutrition(newNutrition)
    }

    fun changeServingSize(newServingSize: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(servingSize = newServingSize)
                ?: NutritionDto(servingSize = newServingSize)
        changeNutrition(newNutrition)
    }

    fun changeSodiumContent(newSodiumContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(sodiumContent = newSodiumContent)
                ?: NutritionDto(sodiumContent = newSodiumContent)
        changeNutrition(newNutrition)
    }

    fun changeSugarContent(newSugarContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(sugarContent = newSugarContent)
                ?: NutritionDto(sugarContent = newSugarContent)
        changeNutrition(newNutrition)
    }

    fun changeTransFatContent(newTransFatContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(transFatContent = newTransFatContent)
                ?: NutritionDto(transFatContent = newTransFatContent)
        changeNutrition(newNutrition)
    }

    fun changeUnsaturatedFatContent(newUnsaturatedFatContent: String) {
        val newNutrition =
            recipeDto.nutrition?.copy(unsaturatedFatContent = newUnsaturatedFatContent)
                ?: NutritionDto(unsaturatedFatContent = newUnsaturatedFatContent)
        changeNutrition(newNutrition)
    }

    fun changeTool(
        index: Int,
        newTool: String,
    ) {
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

    fun changeInstruction(
        index: Int,
        newInstruction: String,
    ) {
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
                    categories =
                        categoriesResponse.value
                            .filter { it.recipeCount > 0 }
                            .filter { it.name != "*" }
                            .map { it.toCategory() }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun getKeywords() {
        getAllKeywordsUseCase.invoke().onEach { keywords = it }.launchIn(viewModelScope)
    }

    private fun changeNutrition(newNutrition: NutritionDto) {
        _uiState.value.ifSuccess {
            recipeDto = recipeDto.copy(nutrition = newNutrition)
        }
    }

    private fun getRecipe(id: Int) {
        viewModelScope.launch {
            recipeDto =
                recipeRepository.getRecipe(id).also { dto ->
                    dto.toRecipe().also {
                        it.prepTime?.toDurationComponents()?.run { prepTime = this }
                        it.cookTime?.toDurationComponents()?.run { cookTime = this }
                        it.totalTime?.toDurationComponents()?.run { totalTime = this }
                    }
                }
        }
    }
}
