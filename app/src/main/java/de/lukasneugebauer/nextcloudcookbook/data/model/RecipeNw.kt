package de.lukasneugebauer.nextcloudcookbook.data.model

import androidx.core.net.toUri
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.domain.model.Recipe

data class RecipeNw(
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
    val nutrition: NutritionNw?,
) {
    fun toRecipe() = Recipe(
        id = id,
        name = name,
        description = description,
        url = url,
        imageUrl = (BuildConfig.NC_BASE_URL + imageUrl).toUri(),
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