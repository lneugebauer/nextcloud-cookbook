package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category

data class CategoriesScreenState(
    val data: List<Category> = emptyList(),
    val error: String? = null
)