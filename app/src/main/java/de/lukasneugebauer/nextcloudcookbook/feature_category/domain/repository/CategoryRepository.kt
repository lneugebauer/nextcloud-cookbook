package de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository

import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource

interface CategoryRepository {

    suspend fun getCategories(): Resource<List<Category>>
}