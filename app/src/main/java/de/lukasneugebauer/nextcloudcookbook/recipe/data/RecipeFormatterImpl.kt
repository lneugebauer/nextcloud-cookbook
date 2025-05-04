package de.lukasneugebauer.nextcloudcookbook.recipe.data

import android.content.res.Resources
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.RecipeFormatter
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import javax.inject.Inject

class RecipeFormatterImpl
    @Inject
    constructor(private val resources: Resources) : RecipeFormatter {
        override fun format(recipe: Recipe): String {
            return StringBuilder().apply {
                append("${recipe.name}\n\n")

                if (recipe.description.isNotBlank()) {
                    append("${recipe.description}\n\n")
                }

                if (recipe.url.isNotBlank()) {
                    val sourceLabel = resources.getString(R.string.recipe_source)
                    append("$sourceLabel: ${recipe.url}\n\n")
                }

                if (recipe.prepTime?.notZero() == true) {
                    val prepTimeLabel = resources.getString(R.string.recipe_prep_time)
                    val prepTime = resources.getString(R.string.recipe_duration, recipe.prepTime.toMinutes())
                    append("$prepTimeLabel: $prepTime\n")
                }
                if (recipe.cookTime?.notZero() == true) {
                    val cookTimeLabel = resources.getString(R.string.recipe_cook_time)
                    val cookTime = resources.getString(R.string.recipe_duration, recipe.cookTime.toMinutes())
                    append("$cookTimeLabel: $cookTime\n")
                }
                if (recipe.totalTime?.notZero() == true) {
                    val totalTimeLabel = resources.getString(R.string.recipe_total_time)
                    val totalTime = resources.getString(R.string.recipe_duration, recipe.totalTime.toMinutes())
                    append("$totalTimeLabel: $totalTime\n")
                }
                if (recipe.prepTime?.notZero() == true ||
                    recipe.cookTime?.notZero() == true ||
                    recipe.totalTime?.notZero() == true
                ) {
                    append("\n")
                }

                if (recipe.ingredients.isNotEmpty()) {
                    val ingredientsLabel = resources.getQuantityString(R.plurals.recipe_ingredients_servings, recipe.yield, recipe.yield)
                    append("$ingredientsLabel\n")
                    recipe.ingredients.forEachIndexed { index, ingredient ->
                        append("• ${ingredient.value}\n")
                        if (recipe.ingredients.size - 1 == index) append("\n")
                    }
                }

                recipe.nutrition?.run {
                    val nutritionLabel = resources.getString(R.string.recipe_nutrition)
                    append("$nutritionLabel\n")

                    calories?.let {
                        val energyLabel = resources.getString(R.string.recipe_nutrition_calories)
                        append("$energyLabel: $it\n")
                    }
                    cholesterolContent?.let {
                        val cholesterolLabel = resources.getString(R.string.recipe_nutrition_cholesterol)
                        append("$cholesterolLabel: $it\n")
                    }
                    fatContent?.let {
                        val fatLabel = resources.getString(R.string.recipe_nutrition_fat_total)
                        append("$fatLabel: $it\n")
                    }
                    fiberContent?.let {
                        val fiberLabel = resources.getString(R.string.recipe_nutrition_fiber)
                        append("$fiberLabel: $it\n")
                    }
                    saturatedFatContent?.let {
                        val saturatedFatLabel = resources.getString(R.string.recipe_nutrition_saturated_fat)
                        append("$saturatedFatLabel: $it\n")
                    }
                    servingSize?.let {
                        val servingSizeLabel = resources.getString(R.string.recipe_nutrition_serving_size)
                        append("$servingSizeLabel: $it\n")
                    }
                    sodiumContent?.let {
                        val sodiumLabel = resources.getString(R.string.recipe_nutrition_sodium)
                        append("$sodiumLabel: $it\n")
                    }
                    sugarContent?.let {
                        val sugarLabel = resources.getString(R.string.recipe_nutrition_sugar)
                        append("$sugarLabel: $it\n")
                    }
                    transFatContent?.let {
                        val transFatLabel = resources.getString(R.string.recipe_nutrition_trans_fat)
                        append("$transFatLabel: $it\n")
                    }
                    unsaturatedFatContent?.let {
                        val unsaturatedFatLabel = resources.getString(R.string.recipe_nutrition_unsaturated_fat)
                        append("$unsaturatedFatLabel: $it\n")
                    }

                    append("\n")
                }

                if (recipe.tools.isNotEmpty()) {
                    val toolsLabel = resources.getString(R.string.recipe_tools)
                    append("$toolsLabel\n")
                    recipe.tools.forEachIndexed { index, tool ->
                        append("• ${tool.value}\n")
                        if (recipe.tools.size - 1 == index) append("\n")
                    }
                }

                if (recipe.instructions.isNotEmpty()) {
                    val instructionsLabel = resources.getString(R.string.recipe_instructions)
                    append("$instructionsLabel\n")
                    recipe.instructions.forEachIndexed { index, instruction ->
                        append("${index + 1}.) ${instruction.value}")
                        if (recipe.tools.size - 1 != index) append("\n\n")
                    }
                }
            }.toString()
        }
    }
