package de.lukasneugebauer.nextcloudcookbook.recipe.data.repository

import coil3.ImageLoader
import coil3.memory.MemoryCache
import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.data.asDataResult
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeConflictDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeImageUpload
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipeDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class RecipeRepositoryImpl
    @Inject
    constructor(
        private val apiProvider: NcCookbookApiProvider,
        private val imageLoader: ImageLoader,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val preferencesManager: PreferencesManager,
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
    ) : BaseRepository(),
        RecipeRepository {
        private val webDavUserIdMutex = Mutex()

        /** Account key to WebDAV user id, see [getWebDavUserId]. */
        private var cachedWebDavUserId: Pair<String, String>? = null

        override fun getRecipePreviewsFlow(): Flow<DataResult<List<RecipePreview>>> =
            recipePreviewDtosFlow().asDataResult { previews -> previews.orEmpty().map { it.toRecipePreview() } }

        /**
         * Recipes of a single category are filtered out of the full preview list instead of being
         * fetched separately. `GET /recipes` already returns every preview together with its
         * category, so a dedicated request would only duplicate data that is cached locally anyway.
         */
        override fun getRecipePreviewsByCategory(categoryName: String): Flow<DataResult<List<RecipePreview>>> =
            recipePreviewDtosFlow().asDataResult { previews ->
                previews
                    .orEmpty()
                    .filter { it.categoryOrUncategorized == categoryName }
                    .map { it.toRecipePreview() }
            }

        override fun getRecipeFlow(id: String): Flow<DataResult<Recipe>> =
            recipeStore
                .stream(StoreReadRequest.cached(key = id, refresh = false))
                // A missing row means the recipe was deleted between the fetcher's write and this
                // read; there is nothing to show, so leave the previous state alone.
                .asDataResult { dto -> dto?.toRecipe() }

        private fun recipePreviewDtosFlow(): Flow<StoreReadResponse<List<RecipePreviewDto>>> =
            recipePreviewsStore.stream(StoreReadRequest.cached(key = Unit, refresh = false))

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
                    refreshCaches(id = id)
                    Resource.Success(data = id)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    handle409ConflictError(e, recipe.name) ?: handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun uploadRecipeImage(image: RecipeImageUpload): Resource<String> {
            return withContext(ioDispatcher) {
                if (image.fileName.isBlank() || image.mimeType.isBlank() || image.bytes.isEmpty()) {
                    return@withContext Resource.Error(message = UiText.StringResource(R.string.error_invalid_image_payload))
                }

                val preferences = preferencesManager.preferencesFlow.first()
                val ncAccount = preferences.ncAccount
                if (ncAccount.username.isBlank() || ncAccount.token.isBlank() || ncAccount.url.isBlank()) {
                    return@withContext Resource.Error(message = UiText.StringResource(R.string.error_no_account_data))
                }

                val uploadFolderName =
                    preferences.recipeImageUploadFolder.trim().ifEmpty {
                        Constants.DEFAULT_RECIPE_IMAGE_UPLOAD_FOLDER
                    }

                try {
                    val api = apiProvider.getApi()
                    if (api == null) {
                        return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))
                    }
                    val userId = getWebDavUserId(account = ncAccount)
                    val uploadFolderUrl =
                        ncAccount.toWebDavUrl(
                            userId = userId,
                            pathSegments = listOf(uploadFolderName),
                        )
                    val fileUrl =
                        ncAccount.toWebDavUrl(
                            userId = userId,
                            pathSegments = listOf(uploadFolderName, image.fileName),
                        )
                    // Create folder if needed
                    val mkcolResponse = api.createWebDavFolder(uploadFolderUrl.toString())
                    if (!mkcolResponse.isSuccessful && mkcolResponse.code() != HTTP_METHOD_NOT_ALLOWED) {
                        return@withContext handleUploadError(mkcolResponse)
                    }
                    val body = image.bytes.toRequestBody(image.mimeType.toMediaType())
                    val putResponse = api.uploadRecipeImage(fileUrl.toString(), body)
                    if (!putResponse.isSuccessful) {
                        return@withContext handleUploadError(putResponse)
                    }
                    Resource.Success(data = "/$uploadFolderName/${image.fileName}")
                } catch (ce: CancellationException) {
                    throw ce
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

                        val previewsResponse = recipePreviewDtosFlow().first()
                        val previews = if (previewsResponse is StoreReadResponse.Data) previewsResponse.value.orEmpty() else emptyList()
                        previews.firstOrNull { it.idOrNull == recipe.id }
                            ?.imageUrl
                            ?.let { imageUrl ->
                                refreshImageCache(cacheKey = imageUrl)
                            }
                    }

                    refreshCaches(id = recipe.id)

                    Resource.Success(Unit)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    handle409ConflictError(e, recipe.name) ?: handleResponseError(e.fillInStackTrace())
                }
            }
        }

        override suspend fun deleteRecipe(id: String): SimpleResource {
            return withContext(ioDispatcher) {
                val api =
                    apiProvider.getApi()
                        ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

                when (val response = api.deleteRecipe(id)) {
                    is NetworkResponse.Success -> {
                        refreshCaches(id = id, deleted = true)
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
                        refreshCaches(id = response.body.id)
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

        /**
         * Resolves the WebDAV user id of [account], remembering it for subsequent uploads instead
         * of asking the server again for every image.
         *
         * Only a successful lookup is cached. Falling back to the account's username is a guess, so
         * caching it would pin a possibly wrong id for the rest of the session after one transient
         * failure. The cache is keyed on the account, so switching accounts invalidates it.
         */
        private suspend fun getWebDavUserId(account: NcAccount): String =
            webDavUserIdMutex.withLock {
                val accountKey = "${account.url}|${account.username}"
                cachedWebDavUserId?.let { (cachedKey, cachedUserId) ->
                    if (cachedKey == accountKey) return@withLock cachedUserId
                }

                when (val response = apiProvider.getApi()?.getCurrentUser()) {
                    is NetworkResponse.Success ->
                        response.body.ocs.data.id.also {
                            cachedWebDavUserId = accountKey to it
                        }

                    else -> account.username
                }
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

        /**
         * Returns a [Resource.Error] when [e] is an HTTP 409 (Conflict), indicating a recipe with
         * the given [name] already exists. Returns `null` for any other exception so the caller can
         * fall through to the standard error handling.
         *
         * Conflict details are attached via [Resource.Error.data] as a [RecipeConflictDto],
         * including the conflicting recipe's ID if it was found in the local previews cache.
         */
        private suspend fun <T> handle409ConflictError(
            e: Exception,
            name: String,
        ): Resource.Error<T>? =
            if (e is HttpException && e.code() == 409) {
                try {
                    val previews = recipePreviewsStore.get(Unit)
                    val existingRecipe = previews.firstOrNull { it.name == name }

                    val conflictDto =
                        RecipeConflictDto(
                            id = existingRecipe?.id,
                            name = existingRecipe?.name ?: name,
                        )
                    @Suppress("UNCHECKED_CAST")
                    Resource.Error(
                        message = UiText.StringResource(R.string.error_recipe_exists, conflictDto.name as Any),
                        data = conflictDto as T?,
                    )
                } catch (ce: CancellationException) {
                    throw ce
                } catch (previewEx: Exception) {
                    Timber.e(previewEx, "Failed to lookup previews for conflict handling")
                    val conflictDto = RecipeConflictDto(id = null, name = name)
                    @Suppress("UNCHECKED_CAST")
                    Resource.Error(
                        message = UiText.StringResource(R.string.error_recipe_exists, conflictDto.name as Any),
                        data = conflictDto as T?,
                    )
                }
            } else {
                null
            }

        private fun <T> handleUploadError(response: Response<*>): Resource.Error<T> = handleResponseError(t = null, code = response.code())

        /**
         * Refreshes every cache a recipe mutation can invalidate. Refreshing the previews also
         * updates the category list and its counts, since both are derived from the previews.
         *
         * Best effort by design: the server has already accepted the mutation by the time this
         * runs, so a failure here must not be reported as a failed create, update or delete. A
         * stale cache is corrected by the next sync; a spurious error is not.
         *
         * The local eviction runs first for the same reason — it cannot fail on the network, so
         * doing it up front keeps a deleted recipe out of the cache even if the refresh below
         * never lands.
         */
        private suspend fun refreshCaches(
            id: String,
            deleted: Boolean = false,
        ) {
            try {
                if (deleted) {
                    recipeStore.clear(id)
                }
                recipePreviewsStore.fresh(Unit)
                if (!deleted && id != emptyRecipeDto().id) {
                    recipeStore.fresh(id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Failed to refresh caches after mutating recipe $id")
            }
        }

        private companion object {
            const val HTTP_METHOD_NOT_ALLOWED = 405
        }
    }
