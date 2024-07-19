package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

data class DownloadRecipeScreenState(
    val url: String = "",
    val error: UiText? = null,
)
