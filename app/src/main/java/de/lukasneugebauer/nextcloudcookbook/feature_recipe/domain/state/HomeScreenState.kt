package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.HomeScreenDataResult

data class HomeScreenState(
    val loading: Boolean = true,
    val data: List<HomeScreenDataResult>? = null,
    val error: String? = null
)