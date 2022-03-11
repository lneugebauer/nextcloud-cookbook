package de.lukasneugebauer.nextcloudcookbook.core.domain.state

sealed class MainState {
    object Initial: MainState()
    object Authorized: MainState()
    object Unauthorized: MainState()
}
