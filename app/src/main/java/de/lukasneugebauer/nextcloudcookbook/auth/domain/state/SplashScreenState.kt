package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

sealed class SplashScreenState {
    object Initial : SplashScreenState()
    object Authorized : SplashScreenState()
    object Unauthorized : SplashScreenState()
}
