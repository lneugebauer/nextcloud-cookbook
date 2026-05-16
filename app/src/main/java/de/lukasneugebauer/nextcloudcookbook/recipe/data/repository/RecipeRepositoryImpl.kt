package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import coil3.ImageLoader
import coil3.memory.MemoryCache
import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.OkHttpClientProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeImageUpload
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import javax.inject.Inject

class RecipeRepositoryImpl
    @Inject
    constructor(
        private val apiProvider: NcCookbookApiProvider,
        private val imageLoader: ImageLoader,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val preferencesManager: PreferencesManager,
        private val clientProvider: OkHttpClientProvider,
        private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
        private val categoriesStore: CategoriesStore,
    ) : BaseRepository(),
        RecipeRepository {
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

        /**
         * Creates a new recipe on the server.
         *
         * @param recipe The [RecipeDto] containing the recipe data.
         * @return A [Resource] containing the new recipe ID on success, or an error message on failure.
         * A 409 Conflict error usually indicates the recipe name already exists.
         */
        override suspend fun createRecipe(recipe: RecipeDto): Resource<String> {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                try {
                    val id = api.createRecipe(recipe = recipe)
                    refreshCaches(id = id, categoryName = recipe.recipeCategory)
                    Resource.Success(data = id)
                } catch (e: Exception) {
                    handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun uploadRecipeImage(image: RecipeImageUpload): Resource<String> {
            return withContext(ioDispatcher) {
                val ncAccount = preferencesManager.preferencesFlow.first().ncAccount
                if (ncAccount.username.isBlank() || ncAccount.token.isBlank() || ncAccount.url.isBlank()) {
                    return@withContext Resource.Error(message = UiText.StringResource(R.string.error_no_account_data))
                }

                try {
                    val client = clientProvider.getCurrentClient()
                    val authHeader = Credentials.basic(ncAccount.username, ncAccount.token)
                    val userId = getWebDavUserId(fallback = ncAccount.username)
                    val uploadFolderUrl =
                        ncAccount.toWebDavUrl(
                            userId = userId,
                            pathSegments = listOf(RECIPE_IMAGE_UPLOAD_FOLDER),
                        )
                    val fileUrl =
                        ncAccount.toWebDavUrl(
                            userId = userId,
                            pathSegments = listOf(RECIPE_IMAGE_UPLOAD_FOLDER, image.fileName),
                        )

                    client
                        .newCall(
                            Request
                                .Builder()
                                .url(uploadFolderUrl)
                                .header("Authorization", authHeader)
                                .method("MKCOL", null)
                                .build(),
                        ).execute()
                        .use { response ->
                            if (!response.isSuccessful && response.code != HTTP_METHOD_NOT_ALLOWED) {
                                return@withContext handleUploadError(response)
                            }
                        }

                    val body = image.bytes.toRequestBody(image.mimeType.toMediaType())
                    client
                        .newCall(
                            Request
                                .Builder()
                                .url(fileUrl)
                                .header("Authorization", authHeader)
                                .put(body)
                                .build(),
                        ).execute()
                        .use { response ->
                            if (!response.isSuccessful) {
                                return@withContext handleUploadError(response)
                            }
                        }

                    Resource.Success(data = "/$RECIPE_IMAGE_UPLOAD_FOLDER/${image.fileName}")
                } catch (e: Exception) {
                    handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun updateRecipe(recipe: RecipeDto): SimpleResource {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                try {
                    val currentRecipe = getRecipe(id = recipe.id)

                    api.updateRecipe(id = recipe.id, recipe = recipe)
                    if (recipe.image != currentRecipe.image && !recipe.imageUrl.isNullOrBlank()) {
                        refreshImageCache(cacheKey = recipe.imageUrl)

                        getRecipePreviewsFlow()
                            .first()
                            .dataOrNull()
                            ?.firstOrNull { it.id == recipe.id }
                            ?.imageUrl
                            ?.let { imageUrl ->
                                refreshImageCache(cacheKey = imageUrl)
                            }
                    }

                    refreshCaches(id = recipe.id, categoryName = recipe.recipeCategory)

                    if (currentRecipe.recipeCategory != recipe.recipeCategory) {
                        recipePreviewsByCategoryStore.fresh(currentRecipe.recipeCategory)
                        categoriesStore.fresh(Unit)
                    }

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
                    apiProvider.getApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                when (val response = api.deleteRecipe(id)) {
                    is NetworkResponse.Success -> {
                        refreshCaches(id = id, categoryName = categoryName, deleted = true)
                        Resource.Success(Unit)
                    }

                    is NetworkResponse.Error -> {
                        handleResponseError(response.error, response.body?.msg)
                    }
                }
            }
        }

        override suspend fun importRecipe(url: ImportUrlDto): Resource<RecipeDto> {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                when (val response = api.importRecipe(url = url)) {
                    is NetworkResponse.Success -> {
                        refreshCaches(id = response.body.id, categoryName = response.body.recipeCategory)
                        Resource.Success(response.body)
                    }

                    is NetworkResponse.Error -> {
                        handleResponseError(response.error, response.body?.msg)
                    }
                }
            }
        }

        private fun refreshImageCache(cacheKey: String) {
            imageLoader.memoryCache?.remove(MemoryCache.Key(cacheKey))
            imageLoader.diskCache?.remove(cacheKey)
        }

        private suspend fun getWebDavUserId(fallback: String): String =
            when (val response = apiProvider.getApi()?.getCurrentUser()) {
                is NetworkResponse.Success -> response.body.ocs.data.id
                else -> fallback
            }

        private fun NcAccount.toWebDavUrl(
            userId: String,
            pathSegments: List<String>,
        ): HttpUrl {
            val builder =
                url
                    .addSuffix("/")
                    .toHttpUrl()
                    .newBuilder()
                    .addPathSegments("remote.php/dav/files")
                    .addPathSegment(userId)

            pathSegments.forEach { pathSegment ->
                builder.addPathSegment(pathSegment)
            }

            return builder.build()
        }

        private fun <T> handleUploadError(response: Response): Resource.Error<T> = handleResponseError(t = null, code = response.code)

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

        private companion object {
            const val HTTP_METHOD_NOT_ALLOWED = 405
            const val RECIPE_IMAGE_UPLOAD_FOLDER = "Cookbook uploads"
        }
    }
