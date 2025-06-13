package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface StartScreenState {
    object Initial : StartScreenState

    data class Loaded(
        val url: String,
        val allowSelfSignedCertificates: Boolean = false,
        val urlError: UiText? = null,
    ) : StartScreenState

    data class WebViewLogin(val url: String, val allowSelfSignedCertificates: Boolean = false) : StartScreenState

    data class ManualLogin(val url: String, val allowSelfSignedCertificates: Boolean = false) : StartScreenState

    data class Error(val uiText: UiText) : StartScreenState
}
