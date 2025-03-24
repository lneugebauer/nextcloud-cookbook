package de.lukasneugebauer.nextcloudcookbook.category.domain.repository

import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse

interface CategoryRepository {
    fun getCategories(): Flow<StoreReadResponse<List<CategoryDto>>>
}
