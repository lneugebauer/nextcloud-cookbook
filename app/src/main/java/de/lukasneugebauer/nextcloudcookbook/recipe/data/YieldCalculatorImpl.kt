package de.lukasneugebauer.nextcloudcookbook.recipe.data

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.YieldCalculator
import java.text.NumberFormat
import java.util.Locale

class YieldCalculatorImpl(customLocale: Locale? = null) : YieldCalculator {
    private val numberFormat =
        if (customLocale != null) {
            NumberFormat.getNumberInstance(customLocale)
        } else {
            NumberFormat.getNumberInstance()
        }.apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }

    override fun isValidIngredientSyntax(ingredient: String): Boolean {
        val startsWithDoubleHash = ingredient.startsWith(DOUBLE_HASH_PREFIX)
        val hasValidSyntax = SYNTAX_REGEX.matches(ingredient)
        val hasMultipleSeparators = MULTIPLE_SEPARATORS_REGEX.matches(ingredient)

        return startsWithDoubleHash || (hasValidSyntax && !hasMultipleSeparators)
    }

    override fun recalculateIngredients(
        ingredients: List<String>,
        currentYield: Int,
        originalYield: Int,
    ): List<String> {
        return ingredients.map { ingredient ->
            if (ingredient.startsWith(DOUBLE_HASH_PREFIX)) return@map ingredient

            if (originalYield < 1) return@map ingredient

            val matchResult = FRACTION_REGEX.matchEntire(ingredient)
            if (matchResult !== null) {
                val (fractionMatch, wholeNumberPartRaw, numeratorRaw, denominatorRaw) = matchResult.destructured

                val wholeNumberPart = wholeNumberPartRaw.toDoubleOrNull() ?: 0.0
                val numerator = numeratorRaw.toDouble()
                val denominator = denominatorRaw.toDouble()

                val decimalAmount = wholeNumberPart + numerator / denominator
                val newAmount = (decimalAmount / originalYield) * currentYield

                return@map ingredient.replace(fractionMatch, numberFormat.format(newAmount))
            }

            if (isValidIngredientSyntax(ingredient)) {
                val possibleUnit =
                    ingredient.split(" ")
                        .firstOrNull()
                        ?.filter { it.isLetter() } ?: ""
                val amount =
                    ingredient.split(" ")
                        .firstOrNull()
                        ?.filter { it.isDigit() || it == ',' || it == '.' }
                        ?.replace(",", ".")
                        ?.toDoubleOrNull() ?: 0.0
                val unitAndIngredient =
                    ingredient.split(" ")
                        .drop(1)
                        .joinToString(" ")

                val newAmount = numberFormat.format((amount / originalYield) * currentYield)
                return@map "$newAmount$possibleUnit $unitAndIngredient"
            }

            ingredient
        }
    }

    companion object {
        const val DOUBLE_HASH_PREFIX = "## "
        val MULTIPLE_SEPARATORS_REGEX = Regex("""^-?\d+(?:[.,]\d+){2,}.*""")
        val FRACTION_REGEX = Regex("""^((\d+\s+)?(\d+)\s*/\s*(\d+)).*""")
        val SYNTAX_REGEX = Regex("""^\d+(?:\.\d+)?(?:/\d+)?\s?.*$""")
    }
}
