package de.lukasneugebauer.nextcloudcookbook.core.domain.repository

import com.google.gson.stream.MalformedJsonException
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

open class BaseRepository {

    fun <T> handleResponseError(t: Throwable?): Resource.Error<T> {
        Timber.e(t?.stackTraceToString())
        val message = when (t) {
            is HttpException -> when (t.code()) {
                400 -> UiText.StringResource(R.string.error_http_400)
                401 -> UiText.StringResource(R.string.error_http_401)
                403 -> UiText.StringResource(R.string.error_http_403)
                404 -> UiText.StringResource(R.string.error_http_404)
                500 -> UiText.StringResource(R.string.error_http_500)
                503 -> UiText.StringResource(R.string.error_http_503)
                else -> unknownErrorUiText(t)
            }
            is SocketTimeoutException -> UiText.StringResource(R.string.error_timeout)
            is UnknownHostException -> UiText.StringResource(R.string.error_unknown_host)
            is MalformedJsonException -> UiText.StringResource(R.string.error_malformed_json)
            is SSLHandshakeException -> UiText.StringResource(R.string.error_ssl_handshake)
            else -> unknownErrorUiText(t)
        }
        return Resource.Error(message)
    }

    private fun unknownErrorUiText(t: Throwable?): UiText = t?.localizedMessage
        ?.let { UiText.DynamicString(it) }
        ?: run { UiText.StringResource(R.string.error_unknown) }
}
