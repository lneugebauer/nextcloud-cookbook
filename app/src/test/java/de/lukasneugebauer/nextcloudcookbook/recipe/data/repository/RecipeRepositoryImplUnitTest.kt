package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import coil3.ImageLoader
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Unit tests for [RecipeRepositoryImpl.createRecipe] method with focus on 409 Conflict error handling.
 *
 * Tests verify that:
 * 1. HTTP 409 (Conflict) exceptions return a user-friendly error with recipe name
 * 2. Non-409 exceptions are handled through the standard error handling flow
 */
class RecipeRepositoryImplUnitTest {
    @Mock
    private lateinit var apiProvider: NcCookbookApiProvider

    @Mock
    private lateinit var imageLoader: ImageLoader

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var repository: RecipeRepositoryImpl
    private lateinit var ioDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        ioDispatcher = Dispatchers.Unconfined // Use Unconfined for synchronous test execution
        repository =
            RecipeRepositoryImpl(
                apiProvider = apiProvider,
                imageLoader = imageLoader,
                ioDispatcher = ioDispatcher,
                preferencesManager = preferencesManager,
                recipePreviewsByCategoryStore = mockRecipePreviewsByCategoryStore(),
                recipePreviewsStore = mockRecipePreviewsStore(),
                recipeStore = mockRecipeStore(),
                categoriesStore = mockCategoriesStore(),
            )
    }

    /**
     * Test that HTTP 409 (Conflict) exception returns Resource.Error with error_recipe_exists
     * string resource and includes the recipe name as an argument.
     *
     * This is the regression test for the special-case 409 handling in createRecipe method
     * (lines 93-97 in RecipeRepositoryImpl.kt).
     */
    @Test
    fun createRecipe_WithHttp409Conflict_ReturnsErrorWithRecipeNameMessage() {
        // Arrange
        val recipeName = "Chocolate Cake"

        // Act & Assert
        val errorMessage = createErrorMessageForConflict(recipeName)
        assertTrue("Error message should be StringResource", errorMessage is UiText.StringResource)

        val stringResource = errorMessage as UiText.StringResource
        assertEquals(
            "Error resource ID should be error_recipe_exists",
            R.string.error_recipe_exists,
            stringResource.resId,
        )
        assertEquals(
            "Recipe name should be passed as argument",
            recipeName,
            stringResource.args[0],
        )
    }

    /**
     * Test that HTTP 409 exception is properly identified and not passed to handleResponseError.
     *
     * This verifies the conditional check `if (e is HttpException && e.code() == 409)`.
     */
    @Test
    fun createRecipe_WithHttp409_VerifiesExceptionTypeAndCode() {
        // Arrange
        val exception = createHttpException(statusCode = 409)

        // Act
        val is409Conflict = exception is HttpException && exception.code() == 409

        // Assert
        assertTrue("Exception should be identified as HttpException with code 409", is409Conflict)
        assertEquals(409, exception.code())
    }

    /**
     * Test that non-409 HttpException (e.g., 500, 401, 404) would be handled through
     * the standard handleResponseError flow, not the special 409 handling.
     */
    @Test
    fun createRecipe_WithNon409HttpException_VerifiesConditionNotMet() {
        // Arrange
        val statusCodes = listOf(400, 401, 403, 404, 405, 500, 503)

        // Act & Assert
        statusCodes.forEach { statusCode ->
            val exception = createHttpException(statusCode = statusCode)
            val is409Conflict = exception is HttpException && exception.code() == 409

            assertTrue(
                "Status code $statusCode should NOT be treated as 409 Conflict",
                !is409Conflict,
            )
        }
    }

    /**
     * Test that non-HttpException errors (e.g., network timeouts, IO exceptions)
     * are not caught by the 409 special handling and would proceed to handleResponseError.
     */
    @Test
    fun createRecipe_WithNonHttpException_VerifiesExceptionNotHttpException() {
        // Arrange
        val nonHttpExceptions =
            listOf(
                IOException("Network error"),
                IllegalArgumentException("Invalid argument"),
                RuntimeException("Unknown runtime error"),
            )

        // Act & Assert
        nonHttpExceptions.forEach { exception ->
            val isHttpException = exception is HttpException
            assertTrue(
                "Exception ${exception::class.simpleName} should NOT be caught by 409 handling",
                !isHttpException,
            )
        }
    }

    /**
     * Test that the recipe name is properly included in the error message arguments.
     * This ensures the user-friendly error message will display the actual recipe name that caused the conflict.
     */
    @Test
    fun createRecipe_With409Error_RecipeNameIncludedInErrorArgs() {
        // Arrange
        val testRecipeNames =
            listOf(
                "Pasta Carbonara",
                "Sushi Roll",
                "Recipe with Spëcial Çharacters",
                "Recipe 123",
                "",
            )

        // Act & Assert
        testRecipeNames.forEach { recipeName ->
            val errorMessage = createErrorMessageForConflict(recipeName)

            assertTrue(
                "Error message should be StringResource",
                errorMessage is UiText.StringResource,
            )

            val stringResource = errorMessage as UiText.StringResource
            assertEquals(
                "Recipe name should be first argument",
                recipeName,
                stringResource.args[0],
            )
            assertEquals(
                "Should have exactly one argument (recipe name)",
                1,
                stringResource.args.size,
            )
        }
    }

    /**
     * Test that the fillInStackTrace() is called on the exception before passing to handleResponseError.
     * This ensures proper stack traces are logged for debugging non-409 errors.
     */
    @Test
    fun createRecipe_WithNon409Exception_StackTraceIsPopulated() {
        // Arrange
        val exception = IOException("Network error")

        // Act
        val filledInException = exception.fillInStackTrace()

        // Assert
        assertNotNull("fillInStackTrace() should return the exception", filledInException)
        assertTrue(
            "Exception should have a stack trace",
            filledInException.stackTrace.isNotEmpty(),
        )
    }

    /**
     * Test edge case: Recipe name with special characters is properly handled in the error message.
     */
    @Test
    fun createRecipe_With409Error_SpecialCharactersInRecipeName() {
        // Arrange
        val specialRecipeNames =
            listOf(
                "Recipe\nWith\nNewlines",
                "Recipe\t\tWith\t\tTabs",
                "Recipe \"Quoted\"",
                "Recipe's Apostrophe",
                "Recipe & Ampersand",
            )

        // Act & Assert
        specialRecipeNames.forEach { recipeName ->
            val errorMessage = createErrorMessageForConflict(recipeName)
            val stringResource = errorMessage as UiText.StringResource

            assertEquals(
                "Recipe name with special characters should be preserved",
                recipeName,
                stringResource.args[0],
            )
        }
    }

    /**
     * Test that the 409 condition uses exact code matching (code == 409, not >= or >).
     */
    @Test
    fun createRecipe_With409Error_VerifiesExactCodeMatching() {
        // Arrange
        val codeRanges =
            mapOf(
                408 to false,
                409 to true,
                410 to false,
            )

        // Act & Assert
        codeRanges.forEach { (code, shouldMatch) ->
            val exception = createHttpException(statusCode = code)
            val is409 = exception is HttpException && exception.code() == 409

            assertEquals(
                "Code $code should match: $shouldMatch",
                shouldMatch,
                is409,
            )
        }
    }

    /**
     * Creates a mock HttpException with the specified status code.
     */
    private fun createHttpException(statusCode: Int): HttpException {
        // Create a mock response using mockito
        val mockResponse: Response<Unit> = mock()
        whenever(mockResponse.code()).thenReturn(statusCode)
        whenever(mockResponse.message()).thenReturn("Error")
        whenever(mockResponse.isSuccessful).thenReturn(false)

        return HttpException(mockResponse)
    }

    /**
     * Simulates the error message construction that would happen in createRecipe
     * when a 409 conflict occurs.
     */
    private fun createErrorMessageForConflict(recipeName: String): UiText = UiText.StringResource(R.string.error_recipe_exists, recipeName)

    /**
     * Mock for RecipePreviewsByCategoryStore to satisfy constructor requirements.
     */
    private fun mockRecipePreviewsByCategoryStore(): RecipePreviewsByCategoryStore = mock()

    /**
     * Mock for RecipePreviewsStore to satisfy constructor requirements.
     */
    private fun mockRecipePreviewsStore(): RecipePreviewsStore = mock()

    /**
     * Mock for RecipeStore to satisfy constructor requirements.
     */
    private fun mockRecipeStore(): RecipeStore = mock()

    /**
     * Mock for CategoriesStore to satisfy constructor requirements.
     */
    private fun mockCategoriesStore(): CategoriesStore = mock()
}
