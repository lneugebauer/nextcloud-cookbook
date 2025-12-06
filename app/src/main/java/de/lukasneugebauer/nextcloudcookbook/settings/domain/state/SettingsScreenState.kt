package de.lukasneugebauer.nextcloudcookbook.settings.domain.state

sealed interface SettingsScreenState {
    object Initial : SettingsScreenState

    data class Loaded(
        val isStayAwake: Boolean,
        val isShowRecipeSyntaxIndicator: Boolean,
    ) : SettingsScreenState
}
