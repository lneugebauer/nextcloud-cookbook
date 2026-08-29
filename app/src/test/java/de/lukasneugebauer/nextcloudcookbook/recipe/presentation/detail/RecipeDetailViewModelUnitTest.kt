package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import androidx.lifecycle.SavedStateHandle
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Preferences
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.RecipeFormatter
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Locale

/**
 * Unit tests for [RecipeDetailViewModel], covering what the detail screen copies and shares once
 * the servings have been changed.
 *
 * Every source in the fixture is a cold [flowOf] without suspension points, so the unconfined
 * dispatcher drains them during construction and the state is fully populated by the time [setUp]
 * returns. That is what lets the tests be plain synchronous assertions — and it matters, because
 * [RecipeDetailViewModel.increaseYield] counts up from the current state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModelUnitTest {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var recipeFormatter: RecipeFormatter
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var viewModel: RecipeDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        preferencesManager = mock()
        whenever(preferencesManager.preferencesFlow).thenReturn(
            flowOf(
                Preferences(
                    isShowIngredientSyntaxIndicator = false,
                    ncAccount =
                        NcAccount(
                            name = "Alice",
                            username = "alice-login",
                            token = "token",
                            url = "https://cloud.example.com",
                        ),
                    recipeOfTheDay = RecipeOfTheDay(id = "0", updatedAt = LocalDateTime.MIN),
                    allowSelfSignedCertificates = false,
                ),
            ),
        )

        recipeFormatter = mock()

        recipeRepository = mock()
        whenever(recipeRepository.getRecipeFlow("1")).thenReturn(flowOf(DataResult.Success(RECIPE)))
        whenever(recipeRepository.getRecipePreviewsFlow()).thenReturn(flowOf(DataResult.Success(emptyList())))

        viewModel =
            RecipeDetailViewModel(
                preferencesManager = preferencesManager,
                recipeFormatter = recipeFormatter,
                recipeRepository = recipeRepository,
                savedStateHandle = SavedStateHandle(mapOf("recipeId" to "1")),
                yieldCalculator = YieldCalculatorImpl(Locale("en")),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * The share path substitutes the recalculated ingredients and the current yield into the recipe
     * before formatting it. It is correct today; this pins it so it cannot silently regress.
     */
    @Test
    fun getShareText_afterIncreaseYield_usesRecalculatedIngredientsAndCurrentYield() {
        viewModel.increaseYield()

        viewModel.getShareText()

        val captor = argumentCaptor<Recipe>()
        verify(recipeFormatter).format(captor.capture())
        assertEquals(5, captor.firstValue.yield)
        assertEquals(
            listOf("500 g flour", "2.5 eggs", "salt"),
            captor.firstValue.ingredients.map { it.value },
        )
    }

    @Test
    fun getShareText_beforeYieldChange_usesOriginalIngredients() {
        viewModel.getShareText()

        val captor = argumentCaptor<Recipe>()
        verify(recipeFormatter).format(captor.capture())
        assertEquals(4, captor.firstValue.yield)
        assertEquals(
            listOf("400 g flour", "2 eggs", "salt"),
            captor.firstValue.ingredients.map { it.value },
        )
    }

    companion object {
        /**
         * Recalculated amounts have to stay below 1000: [YieldCalculatorImpl] formats through
         * `NumberFormat`, which would turn `1200 g` into `1,200 g`.
         */
        private val RECIPE: Recipe =
            emptyRecipe().copy(
                id = "1",
                name = "Pizza",
                yield = 4,
                ingredients =
                    listOf(
                        Ingredient(id = 0, value = "400 g flour", hasCorrectSyntax = true),
                        Ingredient(id = 1, value = "2 eggs", hasCorrectSyntax = true),
                        Ingredient(id = 2, value = "salt", hasCorrectSyntax = false),
                    ),
            )
    }
}
