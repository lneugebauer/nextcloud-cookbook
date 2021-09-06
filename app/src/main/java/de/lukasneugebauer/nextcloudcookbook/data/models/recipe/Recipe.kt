package de.lukasneugebauer.nextcloudcookbook.data.models.recipe

import android.net.Uri
import androidx.core.net.toUri
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.data.models.nutrition.Nutrition
import de.lukasneugebauer.nextcloudcookbook.data.models.nutrition.NutritionNw

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

data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val imageUrl: Uri,
    val category: String,
    val keywords: List<String>,
    val yield: Int,
    // TODO: 19.08.21 Change prepTime, cookTime and totalTime to time fields.
    val prepTime: String?,
    val cookTime: String?,
    val totalTime: String?,
    val nutrition: Nutrition?,
    val tools: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
    // TODO: 19.08.21 Change createdAt and modifiedAt to date fields.
    val createdAt: String,
    val modifiedAt: String,
)