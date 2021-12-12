package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state

import android.net.Uri

sealed class LoginWebViewState {
    object Gone : LoginWebViewState()
    data class Visible(
        val url: Uri,
    ) : LoginWebViewState()
}