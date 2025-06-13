package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface WebViewScreenState {
    object Initial : WebViewScreenState

    data class Loaded(
        val webViewUrl: String,
    ) : WebViewScreenState

    object Authorized : WebViewScreenState

    data class Error(val uiText: UiText) : WebViewScreenState
}
