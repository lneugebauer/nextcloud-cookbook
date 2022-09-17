package de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import java.time.Duration
import java.util.Collections.emptyList

data class RecipeDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("prepTime")
    val prepTime: String?,
    @SerializedName("cookTime")
    val cookTime: String?,
    @SerializedName("totalTime")
    val totalTime: String?,
    @SerializedName("recipeCategory")
    val recipeCategory: String,
    @SerializedName("keywords")
    val keywords: String?,
    @SerializedName("recipeYield")
    val recipeYield: Int,
    @SerializedName("tool")
    val tool: List<String>,
    @SerializedName("recipeIngredient")
    val recipeIngredient: List<String>,
    @SerializedName("recipeInstructions")
    val recipeInstructions: List<String>,
    @SerializedName("dateCreated")
    val dateCreated: String,
    @SerializedName("dateModified")
    val dateModified: String,
    @SerializedName("printImage")
    val printImage: Boolean,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("nutrition")
    val nutrition: NutritionDto?
) {
    fun toRecipe() = Recipe(
        id = id,
        name = name,
        description = description,
        url = url,
        imageOrigin = image,
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
        modifiedAt = dateModified
    )
}
