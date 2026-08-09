package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import coil3.ImageLoader
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.UNCATEGORIZED_RECIPE
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response

/**
 * Unit tests for [RecipeRepositoryImpl] with focus on 409 Conflict error handling
 * in both [RecipeRepositoryImpl.createRecipe] and [RecipeRepositoryImpl.updateRecipe].
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
    private lateinit var recipeStore: RecipeStore
    private lateinit var recipePreviewsStore: RecipePreviewsStore

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        ioDispatcher = Dispatchers.Unconfined // Use Unconfined for synchronous test execution
        recipeStore = mockRecipeStore()
        recipePreviewsStore = mockRecipePreviewsStore()
        repository =
            RecipeRepositoryImpl(
                apiProvider = apiProvider,
                imageLoader = imageLoader,
                ioDispatcher = ioDispatcher,
                preferencesManager = preferencesManager,
                recipePreviewsStore = recipePreviewsStore,
                recipeStore = recipeStore,
                categoriesStore = mockCategoriesStore(),
            )
    }

    /**
     * Stubs [recipeStore]'s stream so that the suspend `get(id)` extension function
     * (which `RecipeRepositoryImpl.getRecipe` / `updateRecipe` rely on) resolves to [recipe].
     *
     * `RecipeStore.get` is a Store5 extension function, not a mockable interface member,
     * so we stub the underlying `stream(...)` call it reads from instead.
     */
    private fun stubRecipeStoreGet(
        id: String,
        recipe: de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto,
    ) {
        whenever(recipeStore.stream(any())).thenReturn(
            flowOf(
                StoreReadResponse.Data(
                    value = recipe,
                    origin = StoreReadResponseOrigin.Fetcher(id),
                ),
            ),
        )
    }

    /**
     * Test that HTTP 409 (Conflict) exception from API throws a real HttpException
     * and repository.createRecipe() returns Resource.Error with error_recipe_exists
     * string resource and includes the recipe name as an argument.
     *
     * This is the regression test for the special-case 409 handling in createRecipe method.
     */
    @Test
    fun createRecipe_WithHttp409Conflict_ReturnsErrorWithRecipeNameMessage() =
        runBlocking {
            // Arrange
            val recipeName = "Chocolate Cake"
            val recipe = emptyRecipeDto().copy(name = recipeName)
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 409)
            whenever(mockApi.createRecipe(recipe = recipe)).thenThrow(httpException)
            whenever(apiProvider.getApi()).thenReturn(mockApi)

            // Act
            val result = repository.createRecipe(recipe)

            // Assert
            assertTrue("Result should be an error", result is Resource.Error)
            val errorMessage = (result as Resource.Error).message
            assertTrue(
                "Error message should be StringResource",
                errorMessage is UiText.StringResource,
            )

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
     * Test that HTTP 409 with different recipe names properly includes the name in the error message.
     */
    @Test
    fun createRecipe_WithHttp409Conflict_HandlesVariousRecipeNames() =
        runBlocking {
            // Arrange
            val testRecipeNames =
                listOf(
                    "Pasta Carbonara",
                    "Sushi Roll",
                    "Recipe with Spëcial Çharacters",
                    "Recipe 123",
                )
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 409)

            testRecipeNames.forEach { recipeName ->
                val recipe = emptyRecipeDto().copy(name = recipeName)
                whenever(mockApi.createRecipe(recipe = recipe)).thenThrow(httpException)
                whenever(apiProvider.getApi()).thenReturn(mockApi)

                // Act
                val result = repository.createRecipe(recipe)

                // Assert
                assertTrue("Result should be an error", result is Resource.Error)
                val stringResource = (result as Resource.Error).message as UiText.StringResource
                assertEquals(
                    "Recipe name should match",
                    recipeName,
                    stringResource.args[0],
                )
            }
        }

    /**
     * Test that non-409 HttpException (e.g., 500, 401, 404) is handled through
     * the standard handleResponseError flow, NOT the special 409 handling,
     * and returns a generic error message instead of error_recipe_exists.
     */
    @Test
    fun createRecipe_WithNon409HttpException_ReturnsGenericError() =
        runBlocking {
            // Arrange
            val recipeName = "Test Recipe"
            val recipe = emptyRecipeDto().copy(name = recipeName)
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 500)
            whenever(mockApi.createRecipe(recipe = recipe)).thenThrow(httpException)
            whenever(apiProvider.getApi()).thenReturn(mockApi)

            // Act
            val result = repository.createRecipe(recipe)

            // Assert
            assertTrue("Result should be an error", result is Resource.Error)
            val errorMessage = (result as Resource.Error).message
            assertTrue(
                "Error message should be StringResource",
                errorMessage is UiText.StringResource,
            )

            val stringResource = errorMessage as UiText.StringResource
            assertNotEquals(
                "Error resource ID should NOT be error_recipe_exists for non-409 errors",
                R.string.error_recipe_exists,
                stringResource.resId,
            )
        }

    /**
     * Test that multiple different non-409 HTTP error codes are handled through handleResponseError
     * and do NOT return the conflict error message.
     */
    @Test
    fun createRecipe_WithVariousNon409HttpExceptions_ReturnsGenericError() =
        runBlocking {
            // Arrange
            val statusCodes = listOf(400, 401, 403, 404, 405, 500, 503)
            val recipeName = "Test Recipe"

            statusCodes.forEach { statusCode ->
                val recipe = emptyRecipeDto().copy(name = recipeName)
                val mockApi: NcCookbookApi = mock()
                val httpException = createHttpException(statusCode = statusCode)
                whenever(mockApi.createRecipe(recipe = recipe)).thenThrow(httpException)
                whenever(apiProvider.getApi()).thenReturn(mockApi)

                // Act
                val result = repository.createRecipe(recipe)

                // Assert
                assertTrue("Result should be an error for status code $statusCode", result is Resource.Error)
                val stringResource = (result as Resource.Error).message as UiText.StringResource
                assertNotEquals(
                    "Status code $statusCode should NOT use error_recipe_exists",
                    R.string.error_recipe_exists,
                    stringResource.resId,
                )
            }
        }

    /**
     * Test that the recipe name is properly included in the error message arguments.
     * This verifies the 409 error construction includes the recipe name for the user-friendly message.
     */
    @Test
    fun createRecipe_With409Error_RecipeNameIncludedInErrorArgs() =
        runBlocking {
            // Arrange
            val testRecipeNames =
                listOf(
                    "Pasta Carbonara",
                    "Sushi Roll",
                    "Recipe with Spëcial Çharacters",
                    "Recipe 123",
                )

            testRecipeNames.forEach { recipeName ->
                val recipe = emptyRecipeDto().copy(name = recipeName)
                val mockApi: NcCookbookApi = mock()
                val httpException = createHttpException(statusCode = 409)
                whenever(mockApi.createRecipe(recipe = recipe)).thenThrow(httpException)
                whenever(apiProvider.getApi()).thenReturn(mockApi)

                // Act
                val result = repository.createRecipe(recipe)

                // Assert
                assertTrue("Result should be an error", result is Resource.Error)
                val stringResource = (result as Resource.Error).message as UiText.StringResource
                assertEquals(
                    "Recipe name should match",
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
     * Test that HTTP 409 (Conflict) exception from API during update
     * returns Resource.Error with error_recipe_exists string resource
     * and includes the recipe name as an argument.
     */
    @Test
    fun updateRecipe_WithHttp409Conflict_ReturnsErrorWithRecipeNameMessage() =
        runBlocking {
            // Arrange
            val recipeName = "Chocolate Cake"
            val recipe = emptyRecipeDto().copy(name = recipeName)
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 409)
            stubRecipeStoreGet(recipe.id, recipe)
            whenever(mockApi.updateRecipe(id = recipe.id, recipe = recipe)).thenThrow(httpException)
            whenever(apiProvider.getApi()).thenReturn(mockApi)

            // Act
            val result = repository.updateRecipe(recipe)

            // Assert
            assertTrue("Result should be an error", result is Resource.Error)
            val errorMessage = (result as Resource.Error).message
            assertTrue(
                "Error message should be StringResource",
                errorMessage is UiText.StringResource,
            )

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
     * Test that HTTP 409 with different recipe names properly includes the name in the error message
     * when updating a recipe.
     */
    @Test
    fun updateRecipe_WithHttp409Conflict_HandlesVariousRecipeNames() =
        runBlocking {
            // Arrange
            val testRecipeNames =
                listOf(
                    "Pasta Carbonara",
                    "Sushi Roll",
                    "Recipe with Spëcial Çharacters",
                    "Recipe 123",
                )
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 409)

            testRecipeNames.forEach { recipeName ->
                val recipe = emptyRecipeDto().copy(name = recipeName)
                stubRecipeStoreGet(recipe.id, recipe)
                whenever(mockApi.updateRecipe(id = recipe.id, recipe = recipe)).thenThrow(httpException)
                whenever(apiProvider.getApi()).thenReturn(mockApi)

                // Act
                val result = repository.updateRecipe(recipe)

                // Assert
                assertTrue("Result should be an error", result is Resource.Error)
                val stringResource = (result as Resource.Error).message as UiText.StringResource
                assertEquals(
                    "Recipe name should match",
                    recipeName,
                    stringResource.args[0],
                )
            }
        }

    /**
     * Test that non-409 HttpException during update is handled through the standard
     * handleResponseError flow, NOT the special 409 handling.
     */
    @Test
    fun updateRecipe_WithNon409HttpException_ReturnsGenericError() =
        runBlocking {
            // Arrange
            val recipeName = "Test Recipe"
            val recipe = emptyRecipeDto().copy(name = recipeName)
            val mockApi: NcCookbookApi = mock()
            val httpException = createHttpException(statusCode = 500)
            stubRecipeStoreGet(recipe.id, recipe)
            whenever(mockApi.updateRecipe(id = recipe.id, recipe = recipe)).thenThrow(httpException)
            whenever(apiProvider.getApi()).thenReturn(mockApi)

            // Act
            val result = repository.updateRecipe(recipe)

            // Assert
            assertTrue("Result should be an error", result is Resource.Error)
            val errorMessage = (result as Resource.Error).message
            assertTrue(
                "Error message should be StringResource",
                errorMessage is UiText.StringResource,
            )

            val stringResource = errorMessage as UiText.StringResource
            assertNotEquals(
                "Error resource ID should NOT be error_recipe_exists for non-409 errors",
                R.string.error_recipe_exists,
                stringResource.resId,
            )
        }

    /**
     * Test that multiple different non-409 HTTP error codes during update do NOT return
     * the conflict error message.
     */
    @Test
    fun updateRecipe_WithVariousNon409HttpExceptions_ReturnsGenericError() =
        runBlocking {
            // Arrange
            val statusCodes = listOf(400, 401, 403, 404, 405, 500, 503)
            val recipeName = "Test Recipe"

            statusCodes.forEach { statusCode ->
                val recipe = emptyRecipeDto().copy(name = recipeName)
                val mockApi: NcCookbookApi = mock()
                val httpException = createHttpException(statusCode = statusCode)
                stubRecipeStoreGet(recipe.id, recipe)
                whenever(mockApi.updateRecipe(id = recipe.id, recipe = recipe)).thenThrow(httpException)
                whenever(apiProvider.getApi()).thenReturn(mockApi)

                // Act
                val result = repository.updateRecipe(recipe)

                // Assert
                assertTrue("Result should be an error for status code $statusCode", result is Resource.Error)
                val stringResource = (result as Resource.Error).message as UiText.StringResource
                assertNotEquals(
                    "Status code $statusCode should NOT use error_recipe_exists",
                    R.string.error_recipe_exists,
                    stringResource.resId,
                )
            }
        }

    /**
     * Stubs [recipePreviewsStore]'s stream with [previews], as if `GET /recipes` had returned them.
     */
    private fun stubRecipePreviews(previews: List<RecipePreviewDto>) {
        whenever(recipePreviewsStore.stream(any())).thenReturn(
            flowOf(
                StoreReadResponse.Data(
                    value = previews,
                    origin = StoreReadResponseOrigin.SourceOfTruth,
                ),
            ),
        )
    }

    private fun recipePreviewDto(
        id: String,
        category: String?,
    ) = RecipePreviewDto(
        recipeId = null,
        id = id,
        name = "Recipe $id",
        keywords = null,
        category = category,
        dateCreated = null,
        dateModified = null,
        imageUrl = null,
        imagePlaceholderUrl = null,
    )

    @Test
    fun getRecipePreviewsByCategory_WithNamedCategory_ReturnsOnlyThatCategory() =
        runBlocking {
            stubRecipePreviews(
                listOf(
                    recipePreviewDto(id = "1", category = "Dessert"),
                    recipePreviewDto(id = "2", category = "Main"),
                    recipePreviewDto(id = "3", category = "Dessert"),
                    recipePreviewDto(id = "4", category = null),
                ),
            )

            val result = repository.getRecipePreviewsByCategory("Dessert").first().requireData()

            assertEquals(listOf("1", "3"), result.map { it.id })
        }

    /**
     * The server exposes uncategorized recipes under the "*" pseudo category in `GET /categories`,
     * so filtering locally has to map both `null` and blank categories onto it.
     */
    @Test
    fun getRecipePreviewsByCategory_WithUncategorized_ReturnsRecipesWithoutCategory() =
        runBlocking {
            stubRecipePreviews(
                listOf(
                    recipePreviewDto(id = "1", category = "Dessert"),
                    recipePreviewDto(id = "2", category = null),
                    recipePreviewDto(id = "3", category = ""),
                    recipePreviewDto(id = "4", category = "   "),
                ),
            )

            val result = repository.getRecipePreviewsByCategory(UNCATEGORIZED_RECIPE).first().requireData()

            assertEquals(listOf("2", "3", "4"), result.map { it.id })
        }

    @Test
    fun getRecipePreviewsByCategory_WithUnknownCategory_ReturnsEmptyList() =
        runBlocking {
            stubRecipePreviews(listOf(recipePreviewDto(id = "1", category = "Dessert")))

            val result = repository.getRecipePreviewsByCategory("Soup").first().requireData()

            assertTrue(result.isEmpty())
        }

    /**
     * Non-data responses must pass through untouched so consumers still see loading and error states.
     */
    @Test
    fun getRecipePreviewsByCategory_WithErrorResponse_PassesErrorThrough() =
        runBlocking {
            whenever(recipePreviewsStore.stream(any())).thenReturn(
                flowOf(
                    StoreReadResponse.Error.Message(
                        message = "boom",
                        origin = StoreReadResponseOrigin.Fetcher(),
                    ),
                ),
            )

            val result = repository.getRecipePreviewsByCategory("Dessert").first()

            assertTrue(result is StoreReadResponse.Error.Message)
            assertEquals("boom", result.errorMessageOrNull())
        }

    /**
     * Creates a mock HttpException with the specified status code.
     */
    private fun createHttpException(statusCode: Int): HttpException {
        val mockResponse: Response<Unit> = mock()
        whenever(mockResponse.code()).thenReturn(statusCode)
        whenever(mockResponse.message()).thenReturn("Error")
        whenever(mockResponse.isSuccessful).thenReturn(false)

        return HttpException(mockResponse)
    }

    private fun mockRecipePreviewsStore(): RecipePreviewsStore = mock()

    private fun mockRecipeStore(): RecipeStore = mock()

    private fun mockCategoriesStore(): CategoriesStore = mock()
}
