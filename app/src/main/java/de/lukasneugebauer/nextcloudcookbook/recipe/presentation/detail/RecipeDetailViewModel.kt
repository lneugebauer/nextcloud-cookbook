package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.asUiText
import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailState())
    val state: StateFlow<RecipeDetailState> = _state

    val stayAwake: Boolean
        get() = preferencesManager.getStayAwake()

    init {
        val id: Int? = savedStateHandle["recipeId"]
        if (id == null) {
            _state.update { it.copy(error = UiText.StringResource(R.string.error_recipe_id_missing)) }
        } else {
            getRecipe(id)
        }
    }

    private fun getRecipe(id: Int) {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            recipeRepository.getRecipeFlow(id).collect { recipeResponse ->
                when (recipeResponse) {
                    is StoreResponse.Loading -> _state.value = _state.value.copy(loading = true)
                    is StoreResponse.Data -> {
                        val recipe = recipeResponse.value.toRecipe()
                        _state.value = _state.value.copy(
                            currentYield = recipe.yield,
                            data = recipe,
                            loading = false,
                        )
                    }

                    is StoreResponse.NoNewData -> _state.value = _state.value.copy(loading = false)
                    is StoreResponse.Error.Exception -> _state.value = _state.value.copy(
                        error = recipeResponse.errorMessageOrNull()?.asUiText(),
                        loading = false,
                    )

                    is StoreResponse.Error.Message -> _state.value = _state.value.copy(
                        error = recipeResponse.message.asUiText(),
                        loading = false,
                    )
                }
            }
        }
    }

    fun increaseYield() {
        val currentYield = _state.value.currentYield + 1
        recalculateYield(currentYield)
    }

    fun decreaseYield() {
        val currentYield = _state.value.currentYield - 1
        if (currentYield < 1) return
        recalculateYield(currentYield)
    }

    fun resetYield() {
        val recipe = _state.value.data ?: return
        recalculateYield(recipe.yield)
    }

    private fun recalculateYield(currentYield: Int) {
        val recipe = _state.value.data ?: return
        val yieldCalculator = YieldCalculatorImpl()
        _state.value = _state.value.copy(
            calculatedIngredients = yieldCalculator.recalculateIngredients(
                recipe.ingredients,
                currentYield,
                recipe.yield,
            ),
            currentYield = currentYield,
        )
    }

    fun getShareText(
        sourceTitle: String,
        prepTime: (duration: Long) -> String,
        cookTime: (duration: Long) -> String,
        totalTime: (duration: Long) -> String,
        ingredientsTitle: String,
        toolsTitle: String,
        instructionsTitle: String,
    ): String {
        val recipe = _state.value.data ?: return ""

        var textToShare = "${recipe.name}\n\n"
        if (recipe.description.isNotBlank()) {
            textToShare += "${recipe.description}\n\n"
        }

        if (recipe.url.isNotBlank()) {
            textToShare += "$sourceTitle: ${recipe.url}\n\n"
        }

        if (recipe.prepTime?.notZero() == true) {
            textToShare += "${prepTime(recipe.prepTime.toMinutes())}\n"
        }
        if (recipe.cookTime?.notZero() == true) {
            textToShare += "${cookTime(recipe.cookTime.toMinutes())}\n"
        }
        if (recipe.totalTime?.notZero() == true) {
            textToShare += "${totalTime(recipe.totalTime.toMinutes())}\n"
        }
        if (recipe.prepTime?.notZero() == true ||
            recipe.cookTime?.notZero() == true ||
            recipe.totalTime?.notZero() == true
        ) {
            textToShare += "\n"
        }

        if (recipe.ingredients.isNotEmpty()) {
            textToShare += "$ingredientsTitle\n"
            recipe.ingredients.forEachIndexed { index, ingredient ->
                textToShare += "• $ingredient\n"
                if (recipe.ingredients.size - 1 == index) textToShare += "\n"
            }
        }

        if (recipe.tools.isNotEmpty()) {
            textToShare += "$toolsTitle\n"
            recipe.tools.forEachIndexed { index, tool ->
                textToShare += "• $tool\n"
                if (recipe.tools.size - 1 == index) textToShare += "\n"
            }
        }

        if (recipe.instructions.isNotEmpty()) {
            textToShare += "$instructionsTitle\n"
            recipe.instructions.forEachIndexed { index, instruction ->
                textToShare += "${index + 1}.) $instruction"
                if (recipe.tools.size - 1 != index) textToShare += "\n\n"
            }
        }

        return textToShare
    }

    fun deleteRecipe(id: Int, categoryName: String) {
        viewModelScope.launch {
            when (val deleteRecipeResource = recipeRepository.deleteRecipe(id, categoryName)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(deleted = true)
                }

                is Resource.Error -> {
                    _state.value = _state.value.copy(error = deleteRecipeResource.message)
                }
            }
        }
    }
}
