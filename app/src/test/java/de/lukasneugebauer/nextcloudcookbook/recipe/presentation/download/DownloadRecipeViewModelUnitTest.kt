package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

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
        viewModel = DownloadRecipeViewModel(recipeRepository)
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

    /**
     * Mirrors the production code pattern in [RecipeRepositoryImpl.handle409ConflictError]:
     * stores a [RecipeConflictDto] in [Resource.Error.data] via an unchecked generic cast so that
     * the JVM's type erasure keeps the real object reachable at runtime (as the ViewModel expects),
     * while the compiler warning is explicitly suppressed.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> conflictResource(dto: RecipeConflictDto): Resource<T> =
        Resource.Error(message = dto.toUiText(), data = dto as T?)
}
