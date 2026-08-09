package de.lukasneugebauer.nextcloudcookbook.category.domain.repository

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    /**
     * Every category that currently has recipes, derived from the cached recipe previews
     * rather than `GET /categories`, so the counts can never disagree with the recipe list.
     */
    fun getCategories(): Flow<DataResult<List<Category>>>
}
