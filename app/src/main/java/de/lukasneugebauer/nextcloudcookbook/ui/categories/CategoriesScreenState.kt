package de.lukasneugebauer.nextcloudcookbook.ui.categories

import de.lukasneugebauer.nextcloudcookbook.domain.model.Category

data class CategoriesScreenState(
    val data: List<Category> = emptyList(),
    val error: String? = null
)