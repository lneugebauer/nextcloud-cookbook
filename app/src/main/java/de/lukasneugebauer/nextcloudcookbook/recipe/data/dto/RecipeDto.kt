package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Tool
import de.lukasneugebauer.nextcloudcookbook.recipe.util.parseAsDuration
import java.util.Collections.emptyList

data class RecipeDto(
    @SerializedName("id")
    val id: String,
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
    val dateCreated: String?,
    @SerializedName("dateModified")
    val dateModified: String?,
    @SerializedName("printImage")
    val printImage: Boolean?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("nutrition")
    val nutrition: NutritionDto?,
) {
    fun toRecipe() =
        Recipe(
            id = id,
            name = name,
            description = description,
            url = url,
            imageOrigin = image,
            imageUrl = imageUrl ?: "",
            category = recipeCategory,
            keywords = if (keywords.isNullOrEmpty()) emptyList() else keywords.split(","),
            yield = recipeYield,
            prepTime = prepTime.parseAsDuration(),
            cookTime = cookTime.parseAsDuration(),
            totalTime = totalTime.parseAsDuration(),
            nutrition = nutrition?.toNutrition(),
            tools = tool.mapIndexed { index, tool -> Tool(id = index, value = tool) },
            ingredients =
                recipeIngredient.mapIndexed { index, ingredient ->
                    Ingredient(id = index, value = ingredient)
                },
            instructions = recipeInstructions.mapIndexed { index, instruction -> Instruction(id = index, value = instruction) },
            createdAt = dateCreated ?: "",
            modifiedAt = dateModified ?: "",
        )
}
