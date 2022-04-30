package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.repository

import com.google.gson.stream.MalformedJsonException
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.AuthApi
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository.AuthRepository
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class AuthRepositoryImpl(private val authApi: AuthApi) : AuthRepository {

    override suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult> {
        return try {
            val response = authApi.getLoginEndpoint("$baseUrl/login/v2")
            val result = response.toLoginEndpointResult()
            Resource.Success(data = result)
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

    override suspend fun tryLogin(url: String, token: String): Resource<LoginResult> {
        return try {
            val response = authApi.tryLogin(url = url, token = token)
            val result = response.toLoginResult()
            Resource.Success(data = result)
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
