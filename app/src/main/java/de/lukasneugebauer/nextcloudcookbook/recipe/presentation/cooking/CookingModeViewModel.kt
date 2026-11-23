package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.cooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.YieldCalculator
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.CookingModeState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.IngredientStepMatcher
import de.lukasneugebauer.nextcloudcookbook.recipe.util.enrichRecipeLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Loads the recipe shown in cooking mode.
 *
 * Ingredients are recalculated for the servings the user picked on the detail screen, passed in
 * as the `servings` navigation argument. Without a usable value they are shown for the recipe's
 * own yield.
 *
 * The step the user is on is deliberately not held here: it lives in the screen's saveable pager
 * state, which already survives configuration changes and process death.
 */
@HiltViewModel
class CookingModeViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
        savedStateHandle: SavedStateHandle,
        private val yieldCalculator: YieldCalculator,
    ) : ViewModel() {
        private val _state = MutableStateFlow(CookingModeState())
        val state: StateFlow<CookingModeState> = _state.asStateFlow()

        private val requestedServings: Int? = savedStateHandle.get<Int>("servings")?.takeIf { it > 0 }

        init {
            val id: String? = savedStateHandle["recipeId"]
            if (id == null) {
                _state.update {
                    it.copy(loading = false, error = UiText.StringResource(R.string.error_recipe_id_missing))
                }
            } else {
                getRecipe(id)
            }
        }

        private fun getRecipe(id: String) {
            combine(
                recipeRepository.getRecipeFlow(id),
                recipeRepository.getRecipePreviewsFlow(),
            ) { recipeResult, recipePreviewsResult ->
                recipeResult to (recipePreviewsResult as? DataResult.Success)?.data
            }.onEach { (recipeResult, recipePreviews) ->
                when (recipeResult) {
                    is DataResult.Loading -> _state.update { it.copy(loading = true) }

                    is DataResult.Success -> {
                        val recipe = recipeResult.data.enrichRecipeLinks(recipePreviews)
                        val servings = requestedServings ?: recipe.yield
                        val originalIngredients = recipe.ingredients.map { ingredient -> ingredient.value }
                        val ingredients =
                            yieldCalculator
                                .recalculateIngredients(
                                    ingredients = originalIngredients,
                                    currentYield = servings,
                                    originalYield = recipe.yield,
                                ).map { calculatedIngredient -> calculatedIngredient.ingredient }
                        // Recalculation keeps the order, so indices into the original ingredients
                        // also index the recalculated ones.
                        val stepIngredients =
                            recipe.instructions.map { instruction ->
                                IngredientStepMatcher.withSectionHeadings(
                                    ingredients = originalIngredients,
                                    indices = IngredientStepMatcher.ingredientIndicesForStep(originalIngredients, instruction.value),
                                )
                            }
                        _state.update {
                            it.copy(
                                recipeName = recipe.name,
                                instructions = recipe.instructions,
                                ingredients = ingredients,
                                stepIngredients = stepIngredients,
                                servings = servings,
                                loading = false,
                                error = null,
                            )
                        }
                    }

                    is DataResult.Error ->
                        _state.update { it.copy(loading = false, error = recipeResult.message) }
                }
            }.launchIn(viewModelScope)
        }
    }
