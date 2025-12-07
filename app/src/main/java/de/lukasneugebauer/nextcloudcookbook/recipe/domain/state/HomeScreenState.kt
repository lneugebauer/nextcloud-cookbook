package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult

sealed interface HomeScreenState {
    object Initial : HomeScreenState

    data class Loaded(
        val data: List<HomeScreenDataResult>,
    ) : HomeScreenState

    data class Error(
        val uiText: UiText,
    ) : HomeScreenState
}
