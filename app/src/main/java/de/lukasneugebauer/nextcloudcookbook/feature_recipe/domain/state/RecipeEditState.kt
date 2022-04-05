package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

sealed class RecipeEditState {
    object Loading : RecipeEditState()
    data class Success(val recipe: Recipe) : RecipeEditState()
    object Error : RecipeEditState()
}
