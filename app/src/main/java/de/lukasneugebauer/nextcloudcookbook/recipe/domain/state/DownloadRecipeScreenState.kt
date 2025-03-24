package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface DownloadRecipeScreenState {
    data class Initial(val url: String = "") : DownloadRecipeScreenState

    data class Loading(val url: String) : DownloadRecipeScreenState

    data class Loaded(val id: String) : DownloadRecipeScreenState

    data class Error(val url: String, val uiText: UiText) : DownloadRecipeScreenState
}
