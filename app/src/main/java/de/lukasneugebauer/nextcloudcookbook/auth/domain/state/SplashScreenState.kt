package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

sealed interface SplashScreenState {
    object Initial : SplashScreenState

    object Authorized : SplashScreenState

    object Unauthorized : SplashScreenState
}
