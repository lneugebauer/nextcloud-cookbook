package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.tasks.util.IngredientQuantityParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngredientQuantityParserUnitTest {
    @Test
    fun ingredient_WithAmountAndSeparateUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("200 g Mehl")
        assertEquals("200 g", result.quantity)
        assertEquals("Mehl", result.name)
    }

    @Test
    fun ingredient_WithAttachedUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("200g Mehl")
        assertEquals("200g", result.quantity)
        assertEquals("Mehl", result.name)
    }

    @Test
    fun ingredient_WithFractionAndUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("1/2 TL Salz")
        assertEquals("1/2 TL", result.quantity)
        assertEquals("Salz", result.name)
    }

    @Test
    fun ingredient_WithUnicodeFraction_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("½ Zwiebel")
        assertEquals("½", result.quantity)
        assertEquals("Zwiebel", result.name)
    }

    @Test
    fun ingredient_WithMixedNumberAndUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("1 1/2 Tassen Zucker")
        assertEquals("1 1/2 Tassen", result.quantity)
        assertEquals("Zucker", result.name)
    }

    @Test
    fun ingredient_WithRange_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("2-3 Äpfel")
        assertEquals("2-3", result.quantity)
        assertEquals("Äpfel", result.name)
    }

    @Test
    fun ingredient_WithDecimalAmount_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("2,5 kg Kartoffeln")
        assertEquals("2,5 kg", result.quantity)
        assertEquals("Kartoffeln", result.name)
    }

    @Test
    fun ingredient_WithAmountWithoutUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("2 rote Zwiebeln")
        assertEquals("2", result.quantity)
        assertEquals("rote Zwiebeln", result.name)
    }

    @Test
    fun ingredient_WithUnitWithTrailingDot_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("1 Pck. Vanillezucker")
        assertEquals("1 Pck.", result.quantity)
        assertEquals("Vanillezucker", result.name)
    }

    @Test
    fun ingredient_WithEnglishUnit_ReturnsQuantityAndName() {
        val result = IngredientQuantityParser.parse("1 cup flour")
        assertEquals("1 cup", result.quantity)
        assertEquals("flour", result.name)
    }

    @Test
    fun ingredient_WithoutAmount_ReturnsNullQuantity() {
        val result = IngredientQuantityParser.parse("Salz und Pfeffer nach Geschmack")
        assertNull(result.quantity)
        assertEquals("Salz und Pfeffer nach Geschmack", result.name)
    }

    @Test
    fun ingredient_WithAttachedNonUnit_ReturnsNullQuantity() {
        val result = IngredientQuantityParser.parse("1er Springform")
        assertNull(result.quantity)
        assertEquals("1er Springform", result.name)
    }

    @Test
    fun ingredient_WithAmountOnly_ReturnsNullQuantity() {
        val result = IngredientQuantityParser.parse("200 g")
        assertNull(result.quantity)
        assertEquals("200 g", result.name)
    }
}
