package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.data.repository.CategoryRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.mobilenativefoundation.store.store5.Fetcher
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
    fun provideCategoriesStore(apiProvider: ApiProvider): CategoriesStore {
        return StoreBuilder
            .from(
                fetcher =
                    Fetcher.of {
                        apiProvider.getNcCookbookApi()?.getCategories()
                            ?: throw NullPointerException("Nextcloud Cookbook API is null.")
                    },
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(categoriesStore: CategoriesStore): CategoryRepository = CategoryRepositoryImpl(categoriesStore)
}
