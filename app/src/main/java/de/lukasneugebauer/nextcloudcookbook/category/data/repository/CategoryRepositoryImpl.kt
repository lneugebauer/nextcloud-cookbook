package de.lukasneugebauer.nextcloudcookbook.category.data.repository

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.data.asDataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoriesStore: CategoriesStore,
    ) : CategoryRepository {
        override fun getCategories(): Flow<DataResult<List<Category>>> =
            categoriesStore
                .stream(StoreReadRequest.cached(key = Unit, refresh = false))
                .asDataResult { dtos -> dtos.map { it.toCategory() } }
    }
