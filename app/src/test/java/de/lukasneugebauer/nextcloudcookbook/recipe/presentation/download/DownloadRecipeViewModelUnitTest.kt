package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.lifecycle.SavedStateHandle
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeConflictDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.ConflictState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadRecipeViewModelUnitTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var viewModel: DownloadRecipeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        recipeRepository = mock()
        viewModel = createViewModel(SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `importRecipe with 409 conflict updates conflict state to Active and resets uiState to Initial`() =
        runTest {
            val testUrl = "https://example.com/recipe"
            viewModel.updateUrl(testUrl)

            val conflictDto = RecipeConflictDto(id = "recipe-123", name = "Test Recipe")
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(conflictResource(conflictDto))

            viewModel.importRecipe()

            val currentConflict = viewModel.conflict.value
            assertTrue("Conflict should be Active", currentConflict is ConflictState.Active)
            val activeConflict = currentConflict as ConflictState.Active
            assertEquals("Test Recipe", activeConflict.name)
            assertEquals("recipe-123", activeConflict.conflictingRecipeId)

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should reset to Initial", currentUiState is DownloadRecipeScreenState.Initial)
            assertEquals(testUrl, (currentUiState as DownloadRecipeScreenState.Initial).url)
        }

    @Test
    fun `importRecipe success updates uiState to Loaded`() =
        runTest {
            val testUrl = "https://example.com/recipe"
            viewModel.updateUrl(testUrl)

            val importedRecipe: RecipeDto = emptyRecipeDto().copy(id = "imported-456")
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Success(importedRecipe))

            viewModel.importRecipe()

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Loaded", currentUiState is DownloadRecipeScreenState.Loaded)
            assertEquals("imported-456", (currentUiState as DownloadRecipeScreenState.Loaded).id)
        }

    @Test
    fun `importRecipe generic error updates uiState to Error`() =
        runTest {
            val testUrl = "https://example.com/recipe"
            viewModel.updateUrl(testUrl)

            val errorMessage = UiText.StringResource(R.string.error_unknown)
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Error(message = errorMessage, data = null))

            viewModel.importRecipe()

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Error", currentUiState is DownloadRecipeScreenState.Error)
            assertEquals(errorMessage, (currentUiState as DownloadRecipeScreenState.Error).uiText)
        }

    @Test
    fun `dismissConflict sets conflict state to None`() =
        runTest {
            val testUrl = "https://example.com/recipe"
            viewModel.updateUrl(testUrl)

            val conflictDto = RecipeConflictDto(id = "recipe-123", name = "Test Recipe")
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(conflictResource(conflictDto))

            viewModel.importRecipe()
            viewModel.dismissConflict()

            assertEquals(ConflictState.None, viewModel.conflict.value)
        }

    @Test
    fun `shared url is imported automatically and updates uiState to Loaded`() =
        runTest {
            val testUrl = "https://example.com/r"
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Success(emptyRecipeDto().copy(id = "42")))

            val viewModel = createViewModel(SavedStateHandle(mapOf("sharedText" to testUrl)))

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Loaded", currentUiState is DownloadRecipeScreenState.Loaded)
            assertEquals("42", (currentUiState as DownloadRecipeScreenState.Loaded).id)
            verify(recipeRepository).importRecipe(ImportUrlDto(testUrl))
        }

    @Test
    fun `shared text with title imports only the extracted url`() =
        runTest {
            val testUrl = "https://example.com/r"
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Success(emptyRecipeDto().copy(id = "42")))

            createViewModel(SavedStateHandle(mapOf("sharedText" to "Title $testUrl")))

            verify(recipeRepository).importRecipe(ImportUrlDto(testUrl))
        }

    @Test
    fun `shared text without url seeds the form and starts no import`() =
        runTest {
            val sharedText = "just some text"

            val viewModel = createViewModel(SavedStateHandle(mapOf("sharedText" to sharedText)))

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Initial", currentUiState is DownloadRecipeScreenState.Initial)
            assertEquals(sharedText, (currentUiState as DownloadRecipeScreenState.Initial).url)
            verifyNoInteractions(recipeRepository)
        }

    @Test
    fun `without shared text uiState stays Initial with an empty url and starts no import`() =
        runTest {
            val viewModel = createViewModel(SavedStateHandle())

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Initial", currentUiState is DownloadRecipeScreenState.Initial)
            assertEquals("", (currentUiState as DownloadRecipeScreenState.Initial).url)
            verifyNoInteractions(recipeRepository)
        }

    @Test
    fun `already triggered auto import is not repeated after process death`() =
        runTest {
            val testUrl = "https://example.com/r"

            val viewModel =
                createViewModel(
                    SavedStateHandle(mapOf("sharedText" to testUrl, "autoImportTriggered" to true)),
                )

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Initial", currentUiState is DownloadRecipeScreenState.Initial)
            assertEquals(testUrl, (currentUiState as DownloadRecipeScreenState.Initial).url)
            verifyNoInteractions(recipeRepository)
        }

    @Test
    fun `automatic import persists the autoImportTriggered flag`() =
        runTest {
            val testUrl = "https://example.com/r"
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Success(emptyRecipeDto().copy(id = "42")))
            val savedStateHandle = SavedStateHandle(mapOf("sharedText" to testUrl))

            createViewModel(savedStateHandle)

            assertEquals(true, savedStateHandle.get<Boolean>("autoImportTriggered"))
        }

    @Test
    fun `failing automatic import updates uiState to Error and keeps the url`() =
        runTest {
            val testUrl = "https://example.com/r"
            val errorMessage = UiText.StringResource(R.string.error_unknown)
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(Resource.Error(message = errorMessage, data = null))

            val viewModel = createViewModel(SavedStateHandle(mapOf("sharedText" to testUrl)))

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should be Error", currentUiState is DownloadRecipeScreenState.Error)
            assertEquals(testUrl, (currentUiState as DownloadRecipeScreenState.Error).url)
            assertEquals(errorMessage, currentUiState.uiText)
        }

    @Test
    fun `automatic import with 409 conflict updates conflict state to Active and resets uiState to Initial`() =
        runTest {
            val testUrl = "https://example.com/r"
            val conflictDto = RecipeConflictDto(id = "recipe-123", name = "Test Recipe")
            whenever(recipeRepository.importRecipe(ImportUrlDto(testUrl)))
                .thenReturn(conflictResource(conflictDto))

            val viewModel = createViewModel(SavedStateHandle(mapOf("sharedText" to testUrl)))

            val currentConflict = viewModel.conflict.value
            assertTrue("Conflict should be Active", currentConflict is ConflictState.Active)
            val activeConflict = currentConflict as ConflictState.Active
            assertEquals("Test Recipe", activeConflict.name)
            assertEquals("recipe-123", activeConflict.conflictingRecipeId)

            val currentUiState = viewModel.uiState.value
            assertTrue("UI state should reset to Initial", currentUiState is DownloadRecipeScreenState.Initial)
            assertEquals(testUrl, (currentUiState as DownloadRecipeScreenState.Initial).url)
        }

    private fun createViewModel(savedStateHandle: SavedStateHandle): DownloadRecipeViewModel =
        DownloadRecipeViewModel(recipeRepository, savedStateHandle)

    /**
     * Mirrors the production code pattern in [RecipeRepositoryImpl.handle409ConflictError]:
     * stores a [RecipeConflictDto] in [Resource.Error.data] via an unchecked generic cast so that
     * the JVM's type erasure keeps the real object reachable at runtime (as the ViewModel expects),
     * while the compiler warning is explicitly suppressed.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> conflictResource(dto: RecipeConflictDto): Resource<T> = Resource.Error(message = dto.toUiText(), data = dto as T?)
}
