package de.lukasneugebauer.nextcloudcookbook.recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe

sealed interface RecipeCreateEditState {
    object Loading : RecipeCreateEditState

    data class Success(
        val recipe: Recipe,
        val prepTime: DurationComponents,
        val cookTime: DurationComponents,
        val totalTime: DurationComponents,
        val categories: List<Category> = emptyList(),
        val keywords: Set<String> = emptySet(),
    ) : RecipeCreateEditState

    data class Updated(val recipeId: String) : RecipeCreateEditState

    data class Error(val error: UiText) : RecipeCreateEditState
}

fun RecipeCreateEditState.ifSuccess(f: () -> Unit) {
    if (this is RecipeCreateEditState.Success) {
        f.invoke()
    }
}
