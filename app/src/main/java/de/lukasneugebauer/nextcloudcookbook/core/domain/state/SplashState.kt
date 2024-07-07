package de.lukasneugebauer.nextcloudcookbook.core.domain.state

sealed class SplashState {
    object Initial : SplashState()

    object Loaded : SplashState()
}
