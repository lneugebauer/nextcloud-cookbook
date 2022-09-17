package de.lukasneugebauer.nextcloudcookbook.category.domain.state

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category

data class CategoryListScreenState(
    val data: List<Category> = emptyList(),
    val error: String? = null
)
