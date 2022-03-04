package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state

import android.net.Uri

data class LoginScreenState(
    val authorized: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val urlError: String? = null,
    val webViewUrl: Uri? = null
)
