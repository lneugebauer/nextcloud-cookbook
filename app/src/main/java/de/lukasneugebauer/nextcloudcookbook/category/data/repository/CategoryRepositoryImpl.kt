package de.lukasneugebauer.nextcloudcookbook.category.data.repository

import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoriesStore: CategoriesStore,
    ) : CategoryRepository {
        override fun getCategories(): Flow<StoreReadResponse<List<CategoryDto>>> {
            return categoriesStore.stream(StoreReadRequest.cached(key = Unit, refresh = false))
        }
    }
