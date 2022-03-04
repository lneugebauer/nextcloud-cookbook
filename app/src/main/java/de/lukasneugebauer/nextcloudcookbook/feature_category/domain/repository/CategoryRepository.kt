package de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository

import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.feature_category.data.dto.CategoryDto
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun getCategories(): Flow<StoreResponse<List<CategoryDto>>>
}
