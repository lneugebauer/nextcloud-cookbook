package de.lukasneugebauer.nextcloudcookbook.category.data.repository

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.data.asDataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.UNCATEGORIZED_RECIPE
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import java.text.Collator
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val recipePreviewsStore: RecipePreviewsStore,
        private val categoriesStore: CategoriesStore,
    ) : CategoryRepository {
        override fun getCategories(): Flow<DataResult<List<Category>>> =
            recipePreviewsStore
                .stream(StoreReadRequest.cached(key = Unit, refresh = false))
                .asDataResult { previews ->
                    previews
                        .orEmpty()
                        .groupingBy { it.categoryOrUncategorized }
                        .eachCount()
                        .map { (name, recipeCount) -> Category(name = name, recipeCount = recipeCount) }
                        .sortedWith(categoryOrder())
                }

        /**
         * Orders the categories the way the list renders them: uncategorized last, because it is a
         * catch-all rather than a category the user named, and the rest by the current locale's
         * collation. A plain `sortedBy { it.name }` compares code points, which puts every
         * capitalised name before every lowercase one and sorts accented names past `Z`.
         */
        private fun categoryOrder(): Comparator<Category> {
            val collator = Collator.getInstance()
            return compareBy<Category> { it.name == UNCATEGORIZED_RECIPE }
                .thenComparator { a, b -> collator.compare(a.name, b.name) }
        }

        override fun getRemoteCategories(): Flow<DataResult<List<Category>>> =
            categoriesStore
                .stream(StoreReadRequest.cached(key = Unit, refresh = true))
                .asDataResult { dtos -> dtos.orEmpty().map { it.toCategory() } }
    }
