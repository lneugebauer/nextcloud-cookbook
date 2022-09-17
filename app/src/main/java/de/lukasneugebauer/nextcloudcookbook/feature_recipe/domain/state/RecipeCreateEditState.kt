package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

sealed interface RecipeCreateEditState {
    object Loading : RecipeCreateEditState
    data class Success(val recipe: Recipe) : RecipeCreateEditState
    data class Updated(val recipeId: Int) : RecipeCreateEditState
    data class Error(val text: String) : RecipeCreateEditState
}

fun RecipeCreateEditState.ifSuccess(f: () -> Unit) {
    if (this is RecipeCreateEditState.Success) {
        f.invoke()
    }
}
