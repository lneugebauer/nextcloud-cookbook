package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto

import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import java.time.Duration
import java.util.Collections.emptyList

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
        keywords = if (keywords == null || keywords.isEmpty()) emptyList() else keywords.split(","),
        yield = recipeYield,
        prepTime = if (prepTime == null || prepTime.isBlank()) null else Duration.parse(prepTime),
        cookTime = if (cookTime == null || cookTime.isBlank()) null else Duration.parse(cookTime),
        totalTime = if (totalTime == null || totalTime.isBlank()) null else Duration.parse(totalTime),
        nutrition = nutrition?.toNutrition(),
        tools = tool,
        ingredients = recipeIngredient,
        instructions = recipeInstructions,
        createdAt = dateCreated,
        modifiedAt = dateModified,
    )
}