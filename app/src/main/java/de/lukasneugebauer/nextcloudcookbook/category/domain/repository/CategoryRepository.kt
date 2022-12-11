package de.lukasneugebauer.nextcloudcookbook.category.domain.repository

import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategories(): Flow<StoreResponse<List<CategoryDto>>>
}
