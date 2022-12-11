package de.lukasneugebauer.nextcloudcookbook.category.domain.state

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

sealed interface CategoryListScreenState {
    object Initial: CategoryListScreenState
    data class Loaded(val data: List<Category>): CategoryListScreenState
    data class Error(val uiText: UiText): CategoryListScreenState
}
