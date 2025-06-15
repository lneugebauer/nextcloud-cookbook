package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface StartScreenState {
    data class Loaded(
        val url: String = "",
        val allowSelfSignedCertificates: Boolean = false,
        val urlError: UiText? = null,
        val event: StartScreenSignInEvent? = null,
    ) : StartScreenState

    data class Error(val uiText: UiText) : StartScreenState
}

sealed interface StartScreenSignInEvent {
    object WebView: StartScreenSignInEvent
    object Manual: StartScreenSignInEvent
}