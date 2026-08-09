package de.lukasneugebauer.nextcloudcookbook.category.domain.repository

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<DataResult<List<Category>>>
}
