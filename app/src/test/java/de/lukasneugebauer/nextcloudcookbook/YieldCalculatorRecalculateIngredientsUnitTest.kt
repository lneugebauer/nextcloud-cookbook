package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CalculatedIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class YieldCalculatorRecalculateIngredientsUnitTest {
    @Test
    fun yieldCalculator_IsValidIngredientSyntax_ReturnsTrue() {
        val yieldCalculator = YieldCalculatorImpl()
        val ingredients =
            listOf(
                "1 cup flour",
                "3tbsp oil",
                "1/2 cup sugar",
                "1 1/2 kg butter",
                "3 1/4 tsp salt",
                "5 bell pepper",
                "1.5 potatoes",
            )
        ingredients.forEach {
            assertTrue(yieldCalculator.isValidIngredientSyntax(it))
        }
    }

    @Test
    fun yieldCalculator_IsValidIngredientSyntax_ReturnsFalse() {
        val yieldCalculator = YieldCalculatorImpl()
        val ingredients =
            listOf(
                "pepper",
                "some oregano",
                "1.500,5 g mushrooms",
                "1,250.50 g beans",
                "1,6 carrots",
                "2-3 bananas",
            )
        ingredients.forEach {
            assertFalse(yieldCalculator.isValidIngredientSyntax(it))
        }
    }

    @Test
    fun yieldCalculator_RecalculateIngredients_ReturnsRecalculatedIngredients() {
        val yieldCalculator = YieldCalculatorImpl(Locale("en"))
        val ingredients =
            listOf(
                "1 cup flour",
                "3tbsp oil",
                "1/2 cup sugar",
                "1 1/2 kg butter",
                "3 1/4 tsp salt",
                "5 bell pepper",
                "pepper",
                "some oregano",
                "1.5 potatoes",
                "1,6 carrots",
                "1 - 2 onions",
                "2-3 bananas",
                "1 150 g apples",
                "¼ unicode",
                "3/8 cup creme",
                "3/5 bananas",
                "0.25 cup yogurt",
                "3 ½ unicode",
            )
        val expectedIngredients =
            listOf(
                CalculatedIngredient("2 cup flour", true),
                CalculatedIngredient("6tbsp oil", true),
                CalculatedIngredient("1 cup sugar", true),
                CalculatedIngredient("3 kg butter", true),
                CalculatedIngredient("6 1/2 tsp salt", true),
                CalculatedIngredient("10 bell pepper", true),
                CalculatedIngredient("pepper", false),
                CalculatedIngredient("some oregano", false),
                CalculatedIngredient("3 potatoes", true),
                CalculatedIngredient("1,6 carrots", false),
                CalculatedIngredient("2 - 2 onions", true),
                CalculatedIngredient("2-3 bananas", false),
                CalculatedIngredient("2 150 g apples", true),
                CalculatedIngredient("1/2 unicode", true),
                CalculatedIngredient("3/4 cup creme", true),
                CalculatedIngredient("1.2 bananas", true),
                CalculatedIngredient("0.5 cup yogurt", true),
                CalculatedIngredient("7 unicode", true),
            )

        assertEquals(
            expectedIngredients,
            yieldCalculator.recalculateIngredients(ingredients, 2, 1),
        )
    }
}
