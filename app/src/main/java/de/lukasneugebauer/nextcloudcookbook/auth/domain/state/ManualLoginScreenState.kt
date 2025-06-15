package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface ManualLoginScreenState {
    data class Loaded(
        val username: String = "",
        val password: String = "",
        val usernameError: UiText? = null,
        val passwordError: UiText? = null,
    ) : ManualLoginScreenState

    data class Authenticating(
        val username: String,
        val password: String,
    ) : ManualLoginScreenState

    object Authenticated : ManualLoginScreenState

    data class Error(val uiText: UiText) : ManualLoginScreenState
}
