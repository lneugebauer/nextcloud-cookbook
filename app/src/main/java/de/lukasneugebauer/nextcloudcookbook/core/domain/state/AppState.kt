package de.lukasneugebauer.nextcloudcookbook.core.domain.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class AppState {
    var isBottomBarVisible by mutableStateOf(true)
    var scrollToTopEvent by mutableStateOf(0L)

    fun triggerScrollToTop() {
        scrollToTopEvent = System.currentTimeMillis()
    }
}

val LocalAppState = compositionLocalOf<AppState> { error("AppState not initialized") }
