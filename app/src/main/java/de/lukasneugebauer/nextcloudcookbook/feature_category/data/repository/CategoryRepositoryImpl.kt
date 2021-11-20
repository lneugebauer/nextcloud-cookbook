package de.lukasneugebauer.nextcloudcookbook.feature_category.data.repository

import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.feature_category.data.remote.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoriesStore: CategoriesStore
) : CategoryRepository {

    override suspend fun getCategories(): Flow<StoreResponse<List<CategoryDto>>> {
        return withContext(Dispatchers.IO) {
            categoriesStore.stream(StoreRequest.cached(key = Unit, refresh = false))
        }
    }
}