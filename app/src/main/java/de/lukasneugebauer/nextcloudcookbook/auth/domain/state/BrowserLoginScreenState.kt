package de.lukasneugebauer.nextcloudcookbook.auth.domain.state

import android.net.Uri
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface BrowserLoginScreenState {
    object Initial : BrowserLoginScreenState

    data class Loaded(
        val loginUrl: Uri,
        val browserLaunched: Boolean = false,
    ) : BrowserLoginScreenState

    object Authenticated : BrowserLoginScreenState

    data class Error(
        val uiText: UiText,
    ) : BrowserLoginScreenState
}
