package de.lukasneugebauer.nextcloudcookbook.recipe.domain

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe

interface RecipeFormatter {
    fun format(recipe: Recipe): String
}
