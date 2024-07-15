package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.recipe.data.YieldCalculatorImpl
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
                "1,6 carrots",
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
            )
        val expectedIngredients =
            listOf(
                "2 cup flour",
                "6tbsp oil",
                "1 cup sugar",
                "3 kg butter",
                "6.5 tsp salt",
                "10 bell pepper",
                "pepper",
                "some oregano",
                "3 potatoes",
                "3.2 carrots",
                "2 - 2 onions",
                "4 bananas",
                "2 150 g apples",
            )

        assertEquals(
            yieldCalculator.recalculateIngredients(ingredients, 2, 1),
            expectedIngredients,
        )
    }
}
