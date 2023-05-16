package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import android.net.Uri
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

data class LoginScreenState(
    val authorized: Boolean = false,
    val usernameError: UiText? = null,
    val passwordError: UiText? = null,
    val urlError: UiText? = null,
    val webViewUrl: Uri? = null,
)
