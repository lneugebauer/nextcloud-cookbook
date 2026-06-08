package de.lukasneugebauer.nextcloudcookbook.auth.domain

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber

class WebViewClient(
    private val allowSelfSignedCertificates: Boolean,
    private val onMainFrameError: (errorCode: Int, description: CharSequence?) -> Unit = { _, _ -> },
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean = false

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?,
    ) {
        if (allowSelfSignedCertificates) {
            handler?.proceed()
        } else {
            handler?.cancel()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request != null && request.isForMainFrame) {
            val sanitizedUrl =
                request.url
                    .buildUpon()
                    .clearQuery()
                    .fragment(null)
                    .build()
            val errorCode = error?.errorCode ?: ERROR_UNKNOWN
            val description = error?.description
            Timber.w("Login WebView load error $errorCode: $description @ $sanitizedUrl")
            onMainFrameError(errorCode, description)
        }
    }

    private companion object {
        private const val ERROR_UNKNOWN = -1
    }
}
