package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import javax.inject.Inject

class RecipeRepositoryImpl
    @Inject
    constructor(
        private val apiProvider: ApiProvider,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
    ) : RecipeRepository, BaseRepository() {
        override fun getRecipePreviewsFlow(): Flow<StoreReadResponse<List<RecipePreviewDto>>> =
            recipePreviewsStore.stream(StoreReadRequest.cached(key = Unit, refresh = false))

        override fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreReadResponse<List<RecipePreviewDto>>> =
            recipePreviewsByCategoryStore.stream(
                StoreReadRequest.cached(
                    key = categoryName,
                    refresh = false,
                ),
            )

        override fun getRecipeFlow(id: String): Flow<StoreReadResponse<RecipeDto>> =
            recipeStore.stream(
                StoreReadRequest.cached(key = id, refresh = false),
            )

        override suspend fun getRecipe(id: String): RecipeDto = recipeStore.get(id)

        override suspend fun createRecipe(recipe: RecipeDto): Resource<String> {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getNcCookbookApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                try {
                    val id = api.createRecipe(recipe = recipe)
                    refreshCaches(id = recipe.id, categoryName = recipe.recipeCategory)
                    Resource.Success(data = id)
                } catch (e: Exception) {
                    handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun updateRecipe(recipe: RecipeDto): SimpleResource {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getNcCookbookApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                try {
                    api.updateRecipe(id = recipe.id, recipe = recipe)
                    refreshCaches(id = recipe.id, categoryName = recipe.recipeCategory)
                    Resource.Success(Unit)
                } catch (e: Exception) {
                    handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun deleteRecipe(
            id: String,
            categoryName: String,
        ): SimpleResource {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getNcCookbookApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                when (val response = api.deleteRecipe(id)) {
                    is NetworkResponse.Success -> {
                        refreshCaches(id = id, categoryName = categoryName, deleted = true)
                        Resource.Success(Unit)
                    }
                    is NetworkResponse.Error -> handleResponseError(response.error, response.body?.msg)
                }
            }
        }

        override suspend fun importRecipe(url: ImportUrlDto): Resource<RecipeDto> {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getNcCookbookApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                when (val response = api.importRecipe(url = url)) {
                    is NetworkResponse.Success -> {
                        refreshCaches(id = response.body.id, categoryName = response.body.recipeCategory)
                        Resource.Success(response.body)
                    }
                    is NetworkResponse.Error -> handleResponseError(response.error, response.body?.msg)
                }
            }
        }

        @OptIn(ExperimentalStoreApi::class)
        private suspend fun refreshCaches(
            id: String,
            categoryName: String,
            deleted: Boolean = false,
        ) {
            if (categoryName.isNotBlank()) {
                recipePreviewsByCategoryStore.fresh(categoryName)
            }
            recipePreviewsStore.fresh(Unit)
            if (deleted) {
                // FIXME: Only clear specific recipe. Something like recipeStore.clear(key = id)
                recipeStore.clear()
            } else if (id != emptyRecipeDto().id) {
                recipeStore.fresh(id)
            }
        }
    }
