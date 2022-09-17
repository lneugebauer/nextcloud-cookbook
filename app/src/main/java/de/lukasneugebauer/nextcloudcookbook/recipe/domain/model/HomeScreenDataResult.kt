package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

import androidx.annotation.StringRes

sealed class HomeScreenDataResult {
    data class Row(
        val headline: String,
        val recipes: List<RecipePreview>
    ) : HomeScreenDataResult()

    data class Single(
        @StringRes val headline: Int,
        val recipe: Recipe
    ) : HomeScreenDataResult()
}
