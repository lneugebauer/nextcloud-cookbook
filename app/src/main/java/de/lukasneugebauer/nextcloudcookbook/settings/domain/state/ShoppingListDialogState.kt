package de.lukasneugebauer.nextcloudcookbook.settings.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.model.TaskList

sealed interface ShoppingListDialogState {
    object Hidden : ShoppingListDialogState

    object Loading : ShoppingListDialogState

    data class Loaded(
        val taskLists: List<TaskList>,
        val selectedUrl: String? = null,
    ) : ShoppingListDialogState

    data class Error(
        val message: UiText,
    ) : ShoppingListDialogState
}
