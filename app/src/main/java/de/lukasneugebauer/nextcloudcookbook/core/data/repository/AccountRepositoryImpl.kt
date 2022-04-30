package de.lukasneugebauer.nextcloudcookbook.core.data.repository

import com.google.gson.stream.MalformedJsonException
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

class AccountRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val preferencesManager: PreferencesManager
) : AccountRepository {

    override suspend fun getCapabilities(): Resource<Capabilities> {
        return withContext(Dispatchers.IO) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            try {
                val response = api.getCapabilities()
                Resource.Success(data = response.ocs.data.capabilities.toCapabilities())
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

    override suspend fun getAccount(): Flow<Resource<NcAccount>> {
        return preferencesManager.preferencesFlow
            .distinctUntilChanged()
            .map {
                val account = it.ncAccount
                if (account.username.isBlank() &&
                    account.token.isBlank() &&
                    account.url.isBlank()
                ) {
                    return@map Resource.Error(
                        data = account,
                        message = UiText.StringResource(R.string.error_no_account_data)
                    )
                }

                Resource.Success(data = account)
            }
    }
}
