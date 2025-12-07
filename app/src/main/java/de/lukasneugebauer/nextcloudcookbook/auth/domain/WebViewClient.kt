package de.lukasneugebauer.nextcloudcookbook.auth.domain

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewClient(
    private val allowSelfSignedCertificates: Boolean,
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
}
