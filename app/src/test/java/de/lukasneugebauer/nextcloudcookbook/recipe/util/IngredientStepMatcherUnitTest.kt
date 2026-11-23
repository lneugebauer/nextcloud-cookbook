package de.lukasneugebauer.nextcloudcookbook.recipe.util

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientStepMatcherUnitTest {
    private fun matches(
        ingredients: List<String>,
        step: String,
    ): List<String> = IngredientStepMatcher.ingredientIndicesForStep(ingredients, step).map { ingredients[it] }

    @Test
    fun englishRecipe_matchesIngredientsNamedInEachStep() {
        val ingredients =
            listOf(
                "2 large onions, diced",
                "3 cloves garlic, minced",
                "2 tbsp olive oil",
                "400 g canned tomatoes",
                "1 tsp salt",
                "250 g spaghetti",
            )

        assertEquals(
            listOf("2 large onions, diced", "2 tbsp olive oil"),
            matches(ingredients, "Heat the oil in a large pan and fry the onion until soft."),
        )
        assertEquals(
            listOf("3 cloves garlic, minced", "400 g canned tomatoes"),
            matches(ingredients, "Add the garlic, then the tomatoes, and simmer for 10 minutes."),
        )
        assertEquals(
            listOf("1 tsp salt", "250 g spaghetti"),
            matches(ingredients, "Cook the spaghetti in salted water. Season with salt."),
        )
    }

    @Test
    fun germanRecipe_matchesInflectionsAndCompounds() {
        val ingredients =
            listOf(
                "500 g Weizenmehl",
                "3 Eier",
                "0,5 l Milch",
                "1 Prise Salz",
                "2 EL Butter",
                "1 große Zwiebel, gehackt",
            )

        assertEquals(
            listOf("500 g Weizenmehl", "3 Eier", "0,5 l Milch"),
            matches(ingredients, "Mehl, Ei und Milch in einer großen Schüssel verrühren."),
        )
        assertEquals(
            listOf("2 EL Butter", "1 große Zwiebel, gehackt"),
            matches(ingredients, "Die Zwiebeln in Butter glasig dünsten."),
        )
        assertEquals(
            listOf("1 Prise Salz"),
            matches(ingredients, "Mit Salz abschmecken."),
        )
    }

    /** Sizes and units are shared by many ingredients and steps; they must not cause matches. */
    @Test
    fun descriptorsAndUnits_doNotMatch() {
        val ingredients = listOf("1 large onion", "1 cup sugar", "1 große Zwiebel")

        assertEquals(
            emptyList<String>(),
            matches(ingredients, "Use a large bowl and a measuring cup. Eine große Schüssel nehmen."),
        )
    }

    @Test
    fun parenthesesAndNotes_areIgnored() {
        val ingredients = listOf("200 g butter (softened, at room temperature)")

        assertEquals(emptyList<String>(), matches(ingredients, "Leave at room temperature."))
        assertEquals(ingredients, matches(ingredients, "Beat the butter."))
    }

    @Test
    fun shortWords_onlyMatchExactly() {
        val ingredients = listOf("2 EL Öl", "1 Ei")

        assertEquals(listOf("2 EL Öl"), matches(ingredients, "Öl erhitzen."))
        assertEquals(emptyList<String>(), matches(ingredients, "Eigelb und Ölsardinen verwenden."))
    }

    @Test
    fun headings_neverMatch() {
        val ingredients = listOf("## Dough", "500 g flour")

        assertEquals(listOf("500 g flour"), matches(ingredients, "Knead the dough and the flour."))
    }

    @Test
    fun recipeLinks_areIgnored() {
        val ingredients = listOf("1 recipe pizza dough")

        assertEquals(
            emptyList<String>(),
            matches(ingredients, "See [#r/7](nccookbook://lneugebauer.github.io/recipe/7)."),
        )
    }

    @Test
    fun withSectionHeadings_addsHeadingOfEachSectionWithAMatch() {
        val ingredients = listOf("## Dough", "flour", "water", "## Topping", "cheese", "## Sauce", "tomatoes")

        assertEquals(
            listOf(0, 2, 5, 6),
            IngredientStepMatcher.withSectionHeadings(ingredients, listOf(2, 6)),
        )
    }

    @Test
    fun withSectionHeadings_withoutHeadings_returnsIndices() {
        assertEquals(listOf(1), IngredientStepMatcher.withSectionHeadings(listOf("flour", "water"), listOf(1)))
    }

    @Test
    fun withSectionHeadings_withoutMatches_returnsEmpty() {
        assertEquals(emptyList<Int>(), IngredientStepMatcher.withSectionHeadings(listOf("## Dough", "flour"), emptyList()))
    }
}
