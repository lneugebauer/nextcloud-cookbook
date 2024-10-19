package de.lukasneugebauer.nextcloudcookbook.recipe.data

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.YieldCalculator
import java.text.Normalizer
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

            // Fraction
            val matchResult = FRACTION_REGEX.matchEntire(ingredient)
            if (matchResult != null) {
                val (fractionMatch, wholeNumberPartRaw, numeratorRaw, denominatorRaw) = matchResult.destructured

                val wholeNumberPart = wholeNumberPartRaw.toDoubleOrNull() ?: 0.0
                val numerator: Double
                val denominator: Double

                // Unicode fraction
                if (numeratorRaw.isBlank()) {
                    val normalizedFraction = Normalizer.normalize(fractionMatch, Normalizer.Form.NFKD)
                    val (numeratorPart, denominatorPart) =
                        normalizedFraction
                            .split("\u2044")
                            .map { it.replace(wholeNumberPartRaw, "") }
                            .map { it.toDouble() }

                    numerator = numeratorPart
                    denominator = denominatorPart
                } else {
                    numerator = numeratorRaw.toDouble()
                    denominator = denominatorRaw.toDouble()
                }

                val decimalAmount = wholeNumberPart + numerator / denominator
                val newAmount = (decimalAmount / originalYield) * currentYield
                val newWholeNumberPart = newAmount.toInt()
                var newNumerator = (newAmount - newWholeNumberPart) * 16
                val newAmountString: String

                if (newNumerator % 1 == 0.0) {
                    fun gcd(
                        a: Int,
                        b: Int,
                    ): Int = if (b == 0) a else gcd(b, a % b)
                    val div = gcd(newNumerator.toInt(), 16)
                    newNumerator /= div
                    val newDenominator = 16 / div
                    val prefix = if (newWholeNumberPart != 0) "$newWholeNumberPart" else ""

                    newAmountString =
                        if (newNumerator == 0.0) {
                            prefix
                        } else if (prefix.isBlank()) {
                            "${newNumerator.toInt()}/$newDenominator"
                        } else {
                            "$prefix ${newNumerator.toInt()}/$newDenominator"
                        }
                } else {
                    newAmountString = numberFormat.format(newAmount).toString()
                }

                return@map ingredient.replace(fractionMatch, newAmountString)
            }

            // Decimal
            if (isValidIngredientSyntax(ingredient)) {
                val possibleUnit =
                    ingredient.split(" ")
                        .firstOrNull()
                        ?.replace(Regex("""[^a-zA-Z]"""), "") ?: ""
                val amount =
                    ingredient.split(" ")
                        .firstOrNull()
                        ?.split("-")
                        ?.firstOrNull()
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
        val FRACTION_REGEX = Regex("""^((\d+\s+)?(?:\p{No}|(\d+)\s*\/\s*(\d+))).*""")
        val SYNTAX_REGEX = Regex("""^(?:(?:\d+\s)?(?:\d+\/\d+|\p{No})|\d+(?:\.\d+)?)[a-zA-z]*\s.*$""")
    }
}
