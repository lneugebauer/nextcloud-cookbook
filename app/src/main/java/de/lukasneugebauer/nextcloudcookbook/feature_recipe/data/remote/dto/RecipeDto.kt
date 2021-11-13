package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

data class RecipeDto(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val image: String,
    val prepTime: String?,
    val cookTime: String?,
    val totalTime: String?,
    val recipeCategory: String,
    val keywords: String?,
    val recipeYield: Int,
    val tool: List<String>,
    val recipeIngredient: List<String>,
    val recipeInstructions: List<String>,
    val dateCreated: String,
    val dateModified: String,
    val printImage: Boolean,
    val imageUrl: String,
    val nutrition: NutritionDto?,
) {
    fun toRecipe() = Recipe(
        id = id,
        name = name,
        description = description,
        url = url,
        imageUrl = imageUrl,
        category = recipeCategory,
        keywords = keywords?.split(",") ?: emptyList(),
        yield = recipeYield,
        prepTime = prepTime,
        cookTime = cookTime,
        totalTime = totalTime,
        nutrition = nutrition?.toNutrition(),
        tools = tool,
        ingredients = recipeIngredient,
        instructions = recipeInstructions,
        createdAt = dateCreated,
        modifiedAt = dateModified,
    )
}