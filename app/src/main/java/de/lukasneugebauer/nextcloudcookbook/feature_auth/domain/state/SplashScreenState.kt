package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state

sealed class SplashScreenState {
    object Initial : SplashScreenState()
    object Authorized : SplashScreenState()
    object Unauthorized : SplashScreenState()
}
