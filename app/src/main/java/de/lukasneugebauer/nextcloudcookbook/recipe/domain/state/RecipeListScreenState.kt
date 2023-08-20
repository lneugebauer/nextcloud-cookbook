package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview

sealed interface RecipeListScreenState {
    object Initial : RecipeListScreenState
    data class Loaded(
        val recipePreviews: List<RecipePreview>,
        val keywords: Set<String>,
    ) : RecipeListScreenState

    data class Error(val uiText: UiText) : RecipeListScreenState
}
