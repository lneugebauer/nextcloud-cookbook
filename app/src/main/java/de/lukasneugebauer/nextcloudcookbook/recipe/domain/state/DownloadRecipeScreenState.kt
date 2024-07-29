package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface DownloadRecipeScreenState {
    data class Initial(val url: String = "") : DownloadRecipeScreenState

    data class Loaded(val id: Int) : DownloadRecipeScreenState

    data class Error(val url: String, val uiText: UiText) : DownloadRecipeScreenState
}
