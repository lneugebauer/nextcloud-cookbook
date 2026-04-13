package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.mapper.toDto
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.mapper.toEntity
import de.lukasneugebauer.nextcloudcookbook.category.data.repository.CategoryRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.category.domain.dao.CategoryDao
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import javax.inject.Singleton

typealias CategoriesStore = Store<Any, List<CategoryDto>>

@Module
@InstallIn(SingletonComponent::class)
object CategoryModule {
    @ExperimentalCoroutinesApi
    @FlowPreview
    @Provides
    @Singleton
    fun provideCategoriesStore(
        apiProvider: NcCookbookApiProvider,
        categoryDao: CategoryDao,
    ): CategoriesStore =
        StoreBuilder
            .from<Any, List<CategoryDto>, List<CategoryDto>>(
                fetcher = Fetcher.of {
                    apiProvider.getApi()?.getCategories()
                        ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                },
                sourceOfTruth = SourceOfTruth.of<Any, List<CategoryDto>, List<CategoryDto>>(
                    reader = { _: Any ->
                        categoryDao.getAll().map { entities ->
                            entities.map { it.toDto() }
                        }
                    },
                    writer = { _: Any, dtos: List<CategoryDto> ->
                        val entities = dtos.map { it.toEntity() }
                        categoryDao.replaceCategories(entities)
                    },
                    delete = { categoryDao.deleteAll() },
                    deleteAll = { categoryDao.deleteAll() },
                )
            ).build()

    @Provides
    @Singleton
    fun provideCategoryRepository(categoriesStore: CategoriesStore): CategoryRepository = CategoryRepositoryImpl(categoriesStore)
}
