package de.lukasneugebauer.nextcloudcookbook.feature_recipe.util

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

fun emptyRecipe(): Recipe = Recipe(
    id = 0,
    name = "",
    description = "",
    url = "",
    imageUrl = "",
    category = "",
    keywords = emptyList(),
    yield = 0,
    prepTime = null,
    cookTime = null,
    totalTime = null,
    nutrition = null,
    tools = emptyList(),
    ingredients = emptyList(),
    instructions = emptyList(),
    createdAt = "",
    modifiedAt = ""
)
