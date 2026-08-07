package de.lukasneugebauer.nextcloudcookbook.tasks.util

import java.util.Locale

data class ParsedIngredient(
    val quantity: String?,
    val name: String,
)

/**
 * Splits an ingredient string like "200 g flour" into its quantity part ("200 g")
 * and its name ("flour"). If no leading quantity is recognized, [ParsedIngredient.quantity]
 * is null and the whole string is treated as the name.
 */
object IngredientQuantityParser {
    private val AMOUNT_REGEX =
        Regex("""^(?:(?:\d+\s+)?\d+\s*/\s*\d+|(?:\d+\s+)?\p{No}|\d+(?:[.,]\d+)?(?:\s*-\s*\d+(?:[.,]\d+)?)?)""")
    private val ATTACHED_UNIT_REGEX = Regex("""^\p{L}+\.?""")

    private val UNITS =
        setOf(
            // German
            "g",
            "gr",
            "gramm",
            "kg",
            "kilogramm",
            "mg",
            "ml",
            "milliliter",
            "cl",
            "dl",
            "l",
            "liter",
            "tl",
            "teelöffel",
            "el",
            "esslöffel",
            "msp",
            "messerspitze",
            "messerspitzen",
            "prise",
            "prisen",
            "pkt",
            "pck",
            "päckchen",
            "packung",
            "packungen",
            "dose",
            "dosen",
            "glas",
            "gläser",
            "becher",
            "bund",
            "zehe",
            "zehen",
            "stück",
            "stk",
            "scheibe",
            "scheiben",
            "blatt",
            "blätter",
            "stange",
            "stangen",
            "würfel",
            "tropfen",
            "schuss",
            "handvoll",
            "tasse",
            "tassen",
            "zweig",
            "zweige",
            "kopf",
            "köpfe",
            "knolle",
            "knollen",
            "portion",
            "portionen",
            "cm",
            // English
            "tsp",
            "teaspoon",
            "teaspoons",
            "tbsp",
            "tbs",
            "tablespoon",
            "tablespoons",
            "cup",
            "cups",
            "oz",
            "ounce",
            "ounces",
            "lb",
            "lbs",
            "pound",
            "pounds",
            "pinch",
            "pinches",
            "clove",
            "cloves",
            "can",
            "cans",
            "slice",
            "slices",
            "stick",
            "sticks",
            "dash",
            "dashes",
            "bunch",
            "bunches",
            "package",
            "packages",
            "packet",
            "packets",
            "piece",
            "pieces",
            "drop",
            "drops",
            "head",
            "heads",
            "sprig",
            "sprigs",
            "quart",
            "quarts",
            "pint",
            "pints",
            "gallon",
            "gallons",
            "stalk",
            "stalks",
        )

    fun parse(ingredient: String): ParsedIngredient {
        val trimmed = ingredient.trim()
        val amountMatch =
            AMOUNT_REGEX.find(trimmed)
                ?: return ParsedIngredient(quantity = null, name = trimmed)
        var quantityEnd = amountMatch.range.last + 1
        val afterAmount = trimmed.substring(quantityEnd)

        if (afterAmount.isNotEmpty() && !afterAmount.first().isWhitespace()) {
            // Unit directly attached to the amount, e.g. "200g flour"
            val attached = ATTACHED_UNIT_REGEX.find(afterAmount)
            if (attached == null || !isUnit(attached.value)) {
                return ParsedIngredient(quantity = null, name = trimmed)
            }
            quantityEnd += attached.value.length
        } else {
            val firstToken =
                trimmed
                    .substring(quantityEnd)
                    .trim()
                    .takeWhile { !it.isWhitespace() }
            if (firstToken.isNotEmpty() && isUnit(firstToken)) {
                quantityEnd = trimmed.indexOf(firstToken, quantityEnd) + firstToken.length
            }
        }

        val name = trimmed.substring(quantityEnd).trim()
        if (name.isEmpty()) {
            return ParsedIngredient(quantity = null, name = trimmed)
        }
        return ParsedIngredient(quantity = trimmed.substring(0, quantityEnd).trim(), name = name)
    }

    private fun isUnit(token: String): Boolean = token.trimEnd('.').lowercase(Locale.ROOT) in UNITS
}
