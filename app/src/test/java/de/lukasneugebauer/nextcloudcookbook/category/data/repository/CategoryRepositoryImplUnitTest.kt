package de.lukasneugebauer.nextcloudcookbook.category.data.repository

import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.UNCATEGORIZED_RECIPE
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [CategoryRepositoryImpl], covering the category list derived from the cached
 * recipe previews and the remote list the create/edit picker reads.
 */
class CategoryRepositoryImplUnitTest {
    private lateinit var recipePreviewsStore: RecipePreviewsStore
    private lateinit var categoriesStore: CategoriesStore
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        recipePreviewsStore = mock()
        categoriesStore = mock()
        repository = CategoryRepositoryImpl(recipePreviewsStore, categoriesStore)
    }

    @Test
    fun getCategories_GroupsThePreviewsAndCountsThem() =
        runBlocking {
            stubPreviews(
                preview(id = "1", category = "Dessert"),
                preview(id = "2", category = "Dessert"),
                preview(id = "3", category = "Main"),
            )

            val result = repository.getCategories().first().successData()

            assertEquals(
                listOf(Category(name = "Dessert", recipeCount = 2), Category(name = "Main", recipeCount = 1)),
                result,
            )
        }

    /**
     * Comparing the names by code point would put every capitalised name before every lowercase
     * one, so "Zucchini" would sort ahead of "apfel".
     */
    @Test
    fun getCategories_SortsCaseInsensitively() =
        runBlocking {
            stubPreviews(
                preview(id = "1", category = "Zucchini"),
                preview(id = "2", category = "apfel"),
                preview(id = "3", category = "Brot"),
            )

            val result = repository.getCategories().first().successData()

            assertEquals(listOf("apfel", "Brot", "Zucchini"), result.map { it.name })
        }

    /**
     * The uncategorized bucket belongs at the end, not wherever "*" happens to sort — and a
     * category that is blank rather than absent belongs in it, since the server treats both the
     * same way.
     */
    @Test
    fun getCategories_GroupsBlankCategoriesUnderUncategorizedAndPutsThemLast() =
        runBlocking {
            stubPreviews(
                preview(id = "1", category = null),
                preview(id = "2", category = ""),
                preview(id = "3", category = "   "),
                preview(id = "4", category = "Dessert"),
            )

            val result = repository.getCategories().first().successData()

            assertEquals(
                listOf(
                    Category(name = "Dessert", recipeCount = 1),
                    Category(name = UNCATEGORIZED_RECIPE, recipeCount = 3),
                ),
                result,
            )
        }

    /**
     * The picker exists to offer categories this device has no recipe for, and `/categories` can
     * report a freshly created one as empty — so an empty category must survive to the picker.
     */
    @Test
    fun getRemoteCategories_KeepsCategoriesWithoutRecipes() =
        runBlocking {
            whenever(categoriesStore.stream(any())).thenReturn(
                flowOf(
                    StoreReadResponse.Data(
                        value = listOf(CategoryDto(name = "Empty", recipeCount = 0)),
                        origin = StoreReadResponseOrigin.Fetcher(),
                    ),
                ),
            )

            val result = repository.getRemoteCategories().first().successData()

            assertEquals(listOf(Category(name = "Empty", recipeCount = 0)), result)
        }

    /**
     * A server without any categories: the reader maps the empty table to `null` so the fetcher runs
     * at all, and Store5 forwards that read after the fetch as `Data(value = null)`. The picker has
     * to end up with an empty list instead of the mapping blowing up on the `null`.
     */
    @Test
    fun getRemoteCategories_WithEmptyCacheAfterFetch_EmitsAnEmptyList() =
        runBlocking {
            @Suppress("UNCHECKED_CAST")
            whenever(categoriesStore.stream(any())).thenReturn(
                flowOf(
                    StoreReadResponse.Data(
                        value = null,
                        origin = StoreReadResponseOrigin.Fetcher(),
                    ) as StoreReadResponse<List<CategoryDto>>,
                ),
            )

            val result = repository.getRemoteCategories().first().successData()

            assertEquals(emptyList<Category>(), result)
        }

    private fun stubPreviews(vararg previews: RecipePreviewDto) {
        whenever(recipePreviewsStore.stream(any())).thenReturn(
            flowOf(
                StoreReadResponse.Data(
                    value = previews.toList(),
                    origin = StoreReadResponseOrigin.SourceOfTruth,
                ),
            ),
        )
    }

    private fun preview(
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

    private fun <T> DataResult<T>.successData(): T = (this as DataResult.Success<T>).data
}
