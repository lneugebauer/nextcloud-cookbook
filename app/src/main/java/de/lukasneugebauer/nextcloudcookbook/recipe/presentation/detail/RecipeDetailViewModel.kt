package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.asUiText
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.RecipeFormatter
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.YieldCalculator
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.StoreReadResponse
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
        private val recipeFormatter: RecipeFormatter,
        private val recipeRepository: RecipeRepository,
        savedStateHandle: SavedStateHandle,
        private val yieldCalculator: YieldCalculator,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RecipeDetailState())
        val state: StateFlow<RecipeDetailState> = _state

        val stayAwake: Boolean
            get() = preferencesManager.getStayAwake()

        init {
            val id: String? = savedStateHandle["recipeId"]
            if (id == null) {
                _state.update { it.copy(error = UiText.StringResource(R.string.error_recipe_id_missing)) }
            } else {
                getRecipe(id)
            }
        }

        private fun getRecipe(id: String) {
            _state.value = _state.value.copy(loading = true)
            combine(
                recipeRepository.getRecipeFlow(id),
                recipeRepository.getRecipePreviewsFlow(),
            ) { recipeResponse, recipePreviewsResponse ->
                val recipePreviewDtos =
                    when (recipePreviewsResponse) {
                        is StoreReadResponse.Data -> recipePreviewsResponse.dataOrNull()
                        else -> null
                    }
                Pair(recipeResponse, recipePreviewDtos)
            }.onEach { (recipeResponse, recipePreviewDtos) ->
                when (recipeResponse) {
                    is StoreReadResponse.Loading -> _state.value = _state.value.copy(loading = true)
                    is StoreReadResponse.Data -> {
                        val recipe = enrichRecipeLinks(recipeResponse.value.toRecipe(), recipePreviewDtos)
                        _state.value =
                            _state.value.copy(
                                calculatedIngredients =
                                    yieldCalculator.recalculateIngredients(
                                        recipe.ingredients.map { it.value },
                                        recipe.yield,
                                        recipe.yield,
                                    ),
                                currentYield = recipe.yield,
                                data = recipe,
                                loading = false,
                            )
                    }

                    is StoreReadResponse.NoNewData -> _state.value = _state.value.copy(loading = false)
                    is StoreReadResponse.Error.Exception ->
                        _state.value =
                            _state.value.copy(
                                error = recipeResponse.errorMessageOrNull()?.asUiText(),
                                loading = false,
                            )

                    is StoreReadResponse.Error.Message ->
                        _state.value =
                            _state.value.copy(
                                error = recipeResponse.message.asUiText(),
                                loading = false,
                            )
                }
            }.launchIn(viewModelScope)
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
            _state.value =
                _state.value.copy(
                    calculatedIngredients =
                        yieldCalculator.recalculateIngredients(
                            recipe.ingredients.map { it.value },
                            currentYield,
                            recipe.yield,
                        ),
                    currentYield = currentYield,
                )
        }

        fun getShareText(): String {
            _state.value.data?.let {
                return recipeFormatter.format(it.copy(
                    yield = _state.value.currentYield,
                    ingredients = _state.value.calculatedIngredients.mapIndexed { index, ingredient ->
                        Ingredient(id = index, value = ingredient)
                    }
                ))
            }
            return ""
        }

        fun deleteRecipe(
            id: String,
            categoryName: String,
        ) {
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

        private fun enrichRecipeLinks(
            recipe: Recipe,
            recipePreviewDtos: List<RecipePreviewDto>?,
        ): Recipe {
            fun replace(text: String): String {
                return RECIPE_URL_REGEX.replace(text) { matchResult ->
                    val (shortId, fullId) = matchResult.destructured

                    if (shortId.isNotBlank()) {
                        val recipePreviewDto =
                            recipePreviewDtos?.firstOrNull { recipePreviewDto ->
                                recipePreviewDto.id == shortId
                            }

                        if (recipePreviewDto?.name?.isNotBlank() == true) {
                            "[${recipePreviewDto.name} (#r/$shortId)](nccookbook://lneugebauer.github.io/recipe/$shortId)"
                        } else {
                            "[#r/$shortId](nccookbook://lneugebauer.github.io/recipe/$shortId)"
                        }
                    } else {
                        "nccookbook://lneugebauer.github.io/recipe/$fullId"
                    }
                }
            }

            val newDescription = replace(recipe.description)

            val newTools =
                recipe.tools.map { tool ->
                    tool.copy(value = replace(tool.value))
                }

            val newIngredients =
                recipe.ingredients.map { ingredient ->
                    ingredient.copy(value = replace(ingredient.value))
                }

            val newInstructions =
                recipe.instructions.map { instruction ->
                    instruction.copy(value = replace(instruction.value))
                }

            return recipe.copy(
                description = newDescription,
                tools = newTools,
                ingredients = newIngredients,
                instructions = newInstructions,
            )
        }

        companion object {
            val RECIPE_URL_REGEX = Regex("""#r/(\d+)|#/recipe/(\d+)""")
        }
    }
