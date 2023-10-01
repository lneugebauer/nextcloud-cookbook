package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState

@Composable
fun HideBottomNavigation() {
    val appState = LocalAppState.current
    DisposableEffect(Unit) {
        appState.isBottomBarVisible = !appState.isBottomBarVisible
        onDispose {
            appState.isBottomBarVisible = !appState.isBottomBarVisible
        }
    }
}
