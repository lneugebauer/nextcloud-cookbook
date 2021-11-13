package de.lukasneugebauer.nextcloudcookbook.feature_category.data.repository

import com.dropbox.android.external.store4.get
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoriesStore: CategoriesStore
) : CategoryRepository {

    override suspend fun getCategories(): Resource<List<Category>> {
        val categories = categoriesStore.get(Unit).map { it.toCategory() }
        return Resource.Success(data = categories)
    }
}