package de.lukasneugebauer.nextcloudcookbook.recipe.util

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe

fun emptyRecipe(): Recipe = Recipe(
    id = 0,
    name = "",
    description = "",
    url = "",
    imageOrigin = "",
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
