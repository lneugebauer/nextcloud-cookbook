package de.lukasneugebauer.nextcloudcookbook.recipe.util

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview

val RECIPE_URL_REGEX = Regex("""#r/(\d+)|#/recipe/(\d+)""")

/**
 * Rewrites Nextcloud Cookbook recipe references (`#r/123` and `#/recipe/123`) in the description,
 * tools, ingredients and instructions into deep links that open the recipe inside the app.
 *
 * Short references become Markdown links labelled with the referenced recipe's name when it is
 * found in [recipePreviews].
 */
fun Recipe.enrichRecipeLinks(recipePreviews: List<RecipePreview>?): Recipe {
    fun replace(text: String): String =
        RECIPE_URL_REGEX.replace(text) { matchResult ->
            val (shortId, fullId) = matchResult.destructured

            if (shortId.isNotBlank()) {
                val recipePreview =
                    recipePreviews?.firstOrNull { recipePreview ->
                        recipePreview.id == shortId
                    }

                if (recipePreview?.name?.isNotBlank() == true) {
                    "[${recipePreview.name} (#r/$shortId)](nccookbook://lneugebauer.github.io/recipe/$shortId)"
                } else {
                    "[#r/$shortId](nccookbook://lneugebauer.github.io/recipe/$shortId)"
                }
            } else {
                "nccookbook://lneugebauer.github.io/recipe/$fullId"
            }
        }

    return copy(
        description = replace(description),
        tools = tools.map { tool -> tool.copy(value = replace(tool.value)) },
        ingredients = ingredients.map { ingredient -> ingredient.copy(value = replace(ingredient.value)) },
        instructions = instructions.map { instruction -> instruction.copy(value = replace(instruction.value)) },
    )
}
