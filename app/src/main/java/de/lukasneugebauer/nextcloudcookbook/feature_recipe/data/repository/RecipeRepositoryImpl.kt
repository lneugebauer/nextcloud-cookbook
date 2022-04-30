package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.repository

import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.google.gson.stream.MalformedJsonException
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class RecipeRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val recipeStore: RecipeStore
) : RecipeRepository {

    private val ncCookbookApi: NcCookbookApi?
        get() = apiProvider.getNcCookbookApi()

    override suspend fun getRecipePreviews(): Flow<StoreResponse<List<RecipePreviewDto>>> {
        return withContext(Dispatchers.IO) {
            recipePreviewsStore.stream(StoreRequest.cached(key = Unit, refresh = false))
        }
    }

    override suspend fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreResponse<List<RecipePreviewDto>>> {
        return withContext(Dispatchers.IO) {
            recipePreviewsByCategoryStore.stream(
                StoreRequest.cached(
                    key = categoryName,
                    refresh = false
                )
            )
        }
    }

    override suspend fun getRecipe(id: Int): Flow<StoreResponse<RecipeDto>> {
        return withContext(Dispatchers.IO) {
            recipeStore.stream(StoreRequest.cached(key = id, refresh = false))
        }
    }

    override suspend fun deleteRecipe(id: Int, categoryName: String): SimpleResource {
        return withContext(Dispatchers.IO) {
            try {
                ncCookbookApi?.deleteRecipe(id)
                recipePreviewsByCategoryStore.clear(categoryName)
                recipePreviewsStore.clear(Unit)
                recipeStore.clear(id)
                Resource.Success(Unit)
            } catch (e: HttpException) {
                Timber.e(e.stackTraceToString())
                val message = when (e.code()) {
                    400 -> UiText.StringResource(R.string.error_http_400)
                    401 -> UiText.StringResource(R.string.error_http_401)
                    403 -> UiText.StringResource(R.string.error_http_403)
                    404 -> UiText.StringResource(R.string.error_http_404)
                    500 -> UiText.StringResource(R.string.error_http_500)
                    503 -> UiText.StringResource(R.string.error_http_503)
                    else -> UiText.StringResource(R.string.error_http_unknown)
                }
                Resource.Error(message)
            } catch (e: SocketTimeoutException) {
                Timber.e(e.stackTraceToString())
                Resource.Error(message = UiText.StringResource(R.string.error_timeout))
            } catch (e: UnknownHostException) {
                Timber.e(e.stackTraceToString())
                Resource.Error(message = UiText.StringResource(R.string.error_unknown_host))
            } catch (e: MalformedJsonException) {
                Timber.e(e.stackTraceToString())
                Resource.Error(message = UiText.StringResource(R.string.error_malformed_json))
            } catch (e: SSLHandshakeException) {
                Timber.e(e.stackTraceToString())
                Resource.Error(message = UiText.StringResource(R.string.error_ssl_handshake))
            } catch (e: Exception) {
                Timber.e(e.stackTraceToString())
                val message = e.localizedMessage?.let { UiText.DynamicString(it) }
                    ?: run { UiText.StringResource(R.string.error_unknown) }
                Resource.Error(message)
            }
        }
    }
}
