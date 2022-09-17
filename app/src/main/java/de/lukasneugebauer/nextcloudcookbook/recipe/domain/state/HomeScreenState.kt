package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult

data class HomeScreenState(
    val loading: Boolean = true,
    val data: List<HomeScreenDataResult>? = null,
    val error: String? = null
)
