package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeSyncState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [SyncRecipesUseCase], covering which recipes it decides to fetch.
 *
 * The point of the use case is that an unchanged library costs one request rather than one
 * per recipe, so most of these assert that `fresh` was *not* called.
 */
class SyncRecipesUseCaseUnitTest {
    private lateinit var recipePreviewsStore: RecipePreviewsStore
    private lateinit var recipeStore: RecipeStore
    private lateinit var recipeDao: RecipeDao
    private lateinit var useCase: SyncRecipesUseCase

    @Before
    fun setUp() {
        recipePreviewsStore = mock()
        recipeStore = mock()
        recipeDao = mock()
        useCase = SyncRecipesUseCase(recipePreviewsStore, recipeStore, recipeDao)
    }

    @Test
    fun invoke_WhenNothingChanged_FetchesNoRecipes() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = "t1"), preview(id = "2", dateModified = "t2"))
            stubCache(RecipeSyncState("1", "t1"), RecipeSyncState("2", "t2"))

            val result = useCase()

            verify(recipeStore, never()).stream(any())
            assertFalse(result.hadFailures)
        }
    }

    @Test
    fun invoke_WhenDateModifiedChanged_FetchesOnlyThatRecipe() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = "t1"), preview(id = "2", dateModified = "t2-new"))
            stubCache(RecipeSyncState("1", "t1"), RecipeSyncState("2", "t2"))
            stubRecipeFetch()

            useCase()

            verifyFetched("2")
            verify(recipeStore, never()).stream(argThat { key == "1" })
        }
    }

    @Test
    fun invoke_WhenRecipeIsNotCached_FetchesIt() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = "t1"), preview(id = "2", dateModified = "t2"))
            stubCache(RecipeSyncState("1", "t1"))
            stubRecipeFetch()

            useCase()

            verifyFetched("2")
        }
    }

    @Test
    fun invoke_WhenRecipeIsGoneFromPreviews_ClearsItWithoutFetching() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = "t1"))
            stubCache(RecipeSyncState("1", "t1"), RecipeSyncState("2", "t2"))

            useCase()

            verify(recipeStore).clear("2")
            verify(recipeStore, never()).stream(any())
        }
    }

    /**
     * A server that omits `dateModified` leaves nothing to compare against. Refetching every
     * sync is exactly what this use case exists to avoid, so a cached recipe is left alone —
     * but one that was never cached still has to be fetched.
     */
    @Test
    fun invoke_WithoutDateModified_FetchesOnlyTheUncachedRecipe() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = null), preview(id = "2", dateModified = null))
            stubCache(RecipeSyncState("1", null))
            stubRecipeFetch()

            useCase()

            verifyFetched("2")
            verify(recipeStore, never()).stream(argThat { key == "1" })
        }
    }

    @Test
    fun invoke_WhenOneFetchFails_StillFetchesTheRestAndReportsFailure() {
        runBlocking {
            stubPreviews(preview(id = "1", dateModified = "t1"), preview(id = "2", dateModified = "t2"))
            stubCache()
            whenever(recipeStore.stream(argThat { key == "1" })).thenThrow(RuntimeException("boom"))
            whenever(recipeStore.stream(argThat { key == "2" })).thenReturn(
                flowOf(StoreReadResponse.Data(value = emptyRecipeDto(), origin = StoreReadResponseOrigin.Fetcher())),
            )

            val result = useCase()

            verifyFetched("2")
            assertTrue(result.hadFailures)
        }
    }

    private suspend fun stubPreviews(vararg previews: RecipePreviewDto) {
        whenever(recipePreviewsStore.stream(any())).thenReturn(
            flowOf(
                StoreReadResponse.Data(
                    value = previews.toList(),
                    origin = StoreReadResponseOrigin.Fetcher(),
                ),
            ),
        )
    }

    private suspend fun stubCache(vararg states: RecipeSyncState) {
        whenever(recipeDao.getSyncStates()).thenReturn(states.toList())
    }

    /** `RecipeStore.fresh(id)` is a Store5 extension, so stub the `stream` it reads from. */
    private fun stubRecipeFetch(recipe: RecipeDto = emptyRecipeDto()) {
        whenever(recipeStore.stream(any())).thenReturn(
            flowOf(
                StoreReadResponse.Data(
                    value = recipe,
                    origin = StoreReadResponseOrigin.Fetcher(),
                ),
            ),
        )
    }

    private fun verifyFetched(id: String) {
        verify(recipeStore).stream(argThat<StoreReadRequest<String>> { key == id })
    }

    private fun preview(
        id: String,
        dateModified: String?,
    ) = RecipePreviewDto(
        recipeId = null,
        id = id,
        name = "Recipe $id",
        keywords = null,
        category = null,
        dateCreated = null,
        dateModified = dateModified,
        imageUrl = null,
        imagePlaceholderUrl = null,
    )
}
