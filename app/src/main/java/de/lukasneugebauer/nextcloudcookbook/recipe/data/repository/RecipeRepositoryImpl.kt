package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
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
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore,
) : RecipeRepository, BaseRepository() {

    override fun getRecipePreviews(): Flow<StoreResponse<List<RecipePreviewDto>>> =
        recipePreviewsStore.stream(StoreRequest.cached(key = Unit, refresh = false))

    override fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreResponse<List<RecipePreviewDto>>> =
        recipePreviewsByCategoryStore.stream(
            StoreRequest.cached(
                key = categoryName,
                refresh = false,
            ),
        )

    override fun getRecipeFlow(id: Int): Flow<StoreResponse<RecipeDto>> =
        recipeStore.stream(StoreRequest.cached(key = id, refresh = false))

    override suspend fun getRecipe(id: Int): RecipeDto = recipeStore.get(id)

    override suspend fun createRecipe(recipe: RecipeDto): Resource<Int> {
        return withContext(ioDispatcher) {
            val api = apiProvider.getNcCookbookApi()
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
            val api = apiProvider.getNcCookbookApi()
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

    override suspend fun deleteRecipe(id: Int, categoryName: String): SimpleResource {
        return withContext(ioDispatcher) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            when (val response = api.deleteRecipe(id)) {
                is NetworkResponse.Success -> {
                    refreshCaches(id = id, categoryName = categoryName, deleted = true)
                    Resource.Success(Unit)
                }
                is NetworkResponse.Error -> handleResponseError(response.error)
            }
        }
    }

    private suspend fun refreshCaches(id: Int, categoryName: String, deleted: Boolean = false) {
        if (categoryName.isNotBlank()) {
            recipePreviewsByCategoryStore.fresh(categoryName)
        }
        recipePreviewsStore.fresh(Unit)
        if (deleted) {
            recipeStore.clear(id)
        } else if (id != emptyRecipeDto().id) {
            recipeStore.fresh(id)
        }
    }
}
