package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state

data class LoginScreenState(
    val authorized: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val urlError: String? = null
)