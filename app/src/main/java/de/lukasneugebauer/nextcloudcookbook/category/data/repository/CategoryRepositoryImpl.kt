package de.lukasneugebauer.nextcloudcookbook.category.data.repository

import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoriesStore: CategoriesStore,
) : CategoryRepository {

    override fun getCategories(): Flow<StoreResponse<List<CategoryDto>>> {
        return categoriesStore.stream(StoreRequest.cached(key = Unit, refresh = false))
    }
}
