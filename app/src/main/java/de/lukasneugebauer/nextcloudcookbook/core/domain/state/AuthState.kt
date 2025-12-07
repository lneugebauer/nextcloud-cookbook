package de.lukasneugebauer.nextcloudcookbook.core.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials

sealed class AuthState {
    object Unauthorized : AuthState()

    data class Authorized(
        val credentials: Credentials,
    ) : AuthState()
}
