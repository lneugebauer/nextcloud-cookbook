package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.cooking

import androidx.lifecycle.SavedStateHandle
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

/**
 * Unit tests for [CookingModeViewModel].
 *
 * As in the detail screen's tests, every source is either a cold [flowOf] or a [MutableStateFlow],
 * which the unconfined dispatcher drains eagerly, so each assertion sees the settled state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CookingModeViewModelUnitTest {
    private lateinit var recipeRepository: RecipeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        recipeRepository = mock()
        whenever(recipeRepository.getRecipePreviewsFlow()).thenReturn(flowOf(DataResult.Success(emptyList())))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        recipeId: String? = "1",
        servings: Int? = null,
    ): CookingModeViewModel =
        CookingModeViewModel(
            recipeRepository = recipeRepository,
            savedStateHandle =
                SavedStateHandle(
                    buildMap {
                        recipeId?.let { put("recipeId", it) }
                        servings?.let { put("servings", it) }
                    },
                ),
            yieldCalculator = YieldCalculatorImpl(Locale("en")),
        )

    @Test
    fun init_withServings_recalculatesIngredientsForThem() {
        givenRecipeFlow(flowOf(DataResult.Success(RECIPE)))

        val state = createViewModel(servings = 4).state.value

        assertEquals(4, state.servings)
        assertEquals(listOf("## Batter", "400 g flour", "4 eggs", "salt"), state.ingredients)
    }

    @Test
    fun init_withoutServings_usesRecipeYield() {
        givenRecipeFlow(flowOf(DataResult.Success(RECIPE)))

        val state = createViewModel().state.value

        assertEquals(2, state.servings)
        assertEquals(listOf("## Batter", "200 g flour", "2 eggs", "salt"), state.ingredients)
    }

    /** The detail screen passes 0 while it has no yield yet; that must not zero the amounts. */
    @Test
    fun init_withZeroServings_usesRecipeYield() {
        givenRecipeFlow(flowOf(DataResult.Success(RECIPE)))

        val state = createViewModel(servings = 0).state.value

        assertEquals(2, state.servings)
        assertEquals("200 g flour", state.ingredients[1])
    }

    @Test
    fun init_withRecipeWithoutYield_showsIngredientsUnchanged() {
        givenRecipeFlow(flowOf(DataResult.Success(RECIPE.copy(yield = 0))))

        val state = createViewModel(servings = 4).state.value

        assertEquals(listOf("## Batter", "200 g flour", "2 eggs", "salt"), state.ingredients)
    }

    private fun givenRecipeFlow(flow: Flow<DataResult<Recipe>>) {
        whenever(recipeRepository.getRecipeFlow("1")).thenReturn(flow)
    }

    @Test
    fun init_withRecipe_exposesNameAndInstructionsInOrder() {
        givenRecipeFlow(flowOf(DataResult.Success(RECIPE)))

        val state = createViewModel().state.value

        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals("Pancakes", state.recipeName)
        assertEquals(listOf("Mix.", "Rest.", "Fry."), state.instructions.map { it.value })
    }

    @Test
    fun init_whileRecipeIsLoading_isLoading() {
        givenRecipeFlow(flowOf(DataResult.Loading))

        val state = createViewModel().state.value

        assertTrue(state.loading)
        assertTrue(state.instructions.isEmpty())
    }

    @Test
    fun init_withRecipeError_exposesError() {
        val message = UiText.DynamicString("Not found")
        givenRecipeFlow(flowOf(DataResult.Error(message)))

        val state = createViewModel().state.value

        assertFalse(state.loading)
        assertEquals(message, state.error)
    }

    /** A failed refresh must not throw away steps the user is already cooking from. */
    @Test
    fun errorAfterSuccess_keepsInstructions() {
        val recipeFlow = MutableStateFlow<DataResult<Recipe>>(DataResult.Success(RECIPE))
        givenRecipeFlow(recipeFlow)
        val viewModel = createViewModel()

        recipeFlow.value = DataResult.Error(UiText.DynamicString("Offline"))

        assertEquals(3, viewModel.state.value.instructions.size)
    }

    @Test
    fun init_withoutRecipeId_exposesMissingIdErrorAndDoesNotLoad() {
        val state = createViewModel(recipeId = null).state.value

        assertFalse(state.loading)
        assertEquals(R.string.error_recipe_id_missing, (state.error as UiText.StringResource).resId)
        verify(recipeRepository, never()).getRecipeFlow("1")
    }

    @Test
    fun init_withRecipeReferenceInInstruction_rewritesItToDeepLink() {
        givenRecipeFlow(
            flowOf(
                DataResult.Success(
                    RECIPE.copy(instructions = listOf(Instruction(id = 0, value = "Serve with #r/7"))),
                ),
            ),
        )
        val syrup =
            RecipePreview(
                id = "7",
                name = "Syrup",
                keywords = emptySet(),
                category = "",
                imageUrl = "",
                createdAt = "",
                modifiedAt = "",
            )
        whenever(recipeRepository.getRecipePreviewsFlow()).thenReturn(flowOf(DataResult.Success(listOf(syrup))))

        val state = createViewModel().state.value

        assertEquals(
            "Serve with [Syrup (#r/7)](nccookbook://lneugebauer.github.io/recipe/7)",
            state.instructions.single().value,
        )
    }

    companion object {
        private val RECIPE =
            emptyRecipe().copy(
                id = "1",
                name = "Pancakes",
                yield = 2,
                ingredients =
                    listOf("## Batter", "200 g flour", "2 eggs", "salt").mapIndexed { index, value ->
                        Ingredient(id = index, value = value, hasCorrectSyntax = true)
                    },
                instructions =
                    listOf(
                        Instruction(id = 0, value = "Mix."),
                        Instruction(id = 1, value = "Rest."),
                        Instruction(id = 2, value = "Fry."),
                    ),
            )
    }
}
