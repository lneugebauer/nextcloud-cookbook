package de.lukasneugebauer.nextcloudcookbook.recipe.util

import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto

fun emptyRecipeDto(): RecipeDto =
    RecipeDto(
        id = "0",
        name = "",
        description = "",
        url = "",
        image = "",
        prepTime = null,
        cookTime = null,
        totalTime = null,
        recipeCategory = "",
        keywords = null,
        recipeYield = 1,
        tool = emptyList(),
        recipeIngredient = emptyList(),
        recipeInstructions = emptyList(),
        dateCreated = "",
        dateModified = "",
        printImage = true,
        imageUrl = "",
        nutrition = null,
    )
