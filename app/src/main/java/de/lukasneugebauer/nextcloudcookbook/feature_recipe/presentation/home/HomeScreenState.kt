package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

data class HomeScreenState(
    val loading: Boolean = true,
    val data: List<HomeScreenData>? = null,
    val error: String? = null
)