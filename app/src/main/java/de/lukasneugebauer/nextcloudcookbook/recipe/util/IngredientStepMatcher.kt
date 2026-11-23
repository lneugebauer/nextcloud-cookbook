package de.lukasneugebauer.nextcloudcookbook.recipe.util

/**
 * Guesses which ingredients a recipe step uses.
 *
 * Recipes do not link ingredients to steps, so this looks for an ingredient's name in the step's
 * text. The name is what is left of the ingredient once quantities, units, preparation notes and
 * common descriptors are removed ("2 large onions, diced" -> "onion"). Words are compared after
 * stripping common English and German plural and inflection endings, and a word also matches a
 * compound that starts or ends with it, so "Mehl" in a step matches "Weizenmehl".
 *
 * This is a heuristic: it misses ingredients a step only implies ("add the dry ingredients") and
 * can match words that merely look alike, so callers should always offer the full list too.
 */
object IngredientStepMatcher {
    private const val HEADING_PREFIX = "##"
    private const val MIN_AFFIX_MATCH_LENGTH = 4
    private const val MIN_STEM_LENGTH = 2

    private val WORD_REGEX = Regex("""\p{L}+""")
    private val URL_REGEX = Regex("""\S+://\S+""")
    private val PARENTHESES_REGEX = Regex("""\([^)]*\)""")

    // A comma right after a word starts a preparation note ("onion, diced"); a comma between
    // digits is a German decimal separator ("0,5 l Milch") and must not split the ingredient.
    private val NOTE_SEPARATOR_REGEX = Regex("""(?<=[\p{L})]),\s""")

    // Longest first, so that "tomatoes" loses "es" rather than just "s".
    private val SUFFIXES = listOf("en", "er", "es", "e", "n", "s")

    private val UNITS =
        setOf(
            // English
            "g",
            "kg",
            "mg",
            "ml",
            "l",
            "cl",
            "dl",
            "oz",
            "lb",
            "lbs",
            "tsp",
            "tbsp",
            "tbs",
            "teaspoon",
            "tablespoon",
            "cup",
            "ounce",
            "pound",
            "gram",
            "kilogram",
            "milliliter",
            "liter",
            "litre",
            "pinch",
            "dash",
            "can",
            "clove",
            "piece",
            "slice",
            "handful",
            "bunch",
            "package",
            "pkg",
            "pack",
            "stick",
            "sprig",
            "jar",
            "glass",
            // German
            "el",
            "tl",
            "msp",
            "prise",
            "stück",
            "stk",
            "scheibe",
            "zehe",
            "bund",
            "dose",
            "packung",
            "päckchen",
            "pck",
            "pkt",
            "becher",
            "glas",
            "tasse",
            "handvoll",
            "gramm",
            "kilogramm",
            "milliliter",
            "stange",
            "blatt",
            "zweig",
            "würfel",
        ).mapTo(HashSet(), ::stem)

    private val DESCRIPTORS =
        setOf(
            // English
            "a",
            "an",
            "and",
            "or",
            "of",
            "the",
            "to",
            "for",
            "with",
            "some",
            "few",
            "about",
            "approx",
            "optional",
            "large",
            "small",
            "medium",
            "big",
            "fresh",
            "freshly",
            "ripe",
            "whole",
            "chopped",
            "diced",
            "sliced",
            "minced",
            "ground",
            "grated",
            "finely",
            "roughly",
            "room",
            "temperature",
            "extra",
            "cold",
            "warm",
            "hot",
            "dried",
            "frozen",
            "cooked",
            "raw",
            "peeled",
            "taste",
            "more",
            "plus",
            // German
            "und",
            "oder",
            "von",
            "der",
            "die",
            "das",
            "ein",
            "eine",
            "mit",
            "für",
            "zum",
            "zur",
            "etwas",
            "ca",
            "evtl",
            "nach",
            "belieben",
            "geschmack",
            "groß",
            "klein",
            "mittelgroß",
            "frisch",
            "gehackt",
            "gewürfelt",
            "geschnitten",
            "gerieben",
            "gemahlen",
            "fein",
            "grob",
            "zimmertemperatur",
            "weich",
            "kalt",
            "warm",
            "heiß",
            "getrocknet",
            "tiefgekühlt",
            "gekocht",
            "geschält",
            "optional",
            "mehr",
        ).mapTo(HashSet(), ::stem)

    /** Returns the indices of the [ingredients] the [step] seems to use. Headings never match. */
    fun ingredientIndicesForStep(
        ingredients: List<String>,
        step: String,
    ): List<Int> {
        val stepWords = words(step).toSet()
        if (stepWords.isEmpty()) return emptyList()

        return ingredients.indices.filter { index ->
            ingredientKeywords(ingredients[index]).any { keyword ->
                stepWords.any { stepWord -> wordsMatch(keyword, stepWord) }
            }
        }
    }

    /**
     * Returns [indices] together with the heading ("## Dough") of every section that one of them
     * belongs to, in recipe order, so a filtered list keeps its section context.
     */
    fun withSectionHeadings(
        ingredients: List<String>,
        indices: List<Int>,
    ): List<Int> {
        if (indices.isEmpty()) return emptyList()
        val selected = indices.toSet()
        val result = mutableListOf<Int>()
        var pendingHeading: Int? = null
        ingredients.forEachIndexed { index, ingredient ->
            when {
                ingredient.startsWith(HEADING_PREFIX) -> pendingHeading = index

                index in selected -> {
                    pendingHeading?.let(result::add)
                    pendingHeading = null
                    result.add(index)
                }
            }
        }
        return result
    }

    internal fun ingredientKeywords(ingredient: String): Set<String> {
        if (ingredient.startsWith(HEADING_PREFIX)) return emptySet()
        val name =
            ingredient
                .replace(PARENTHESES_REGEX, " ")
                .split(NOTE_SEPARATOR_REGEX, limit = 2)
                .first()
        return words(name).filterNot { it in UNITS || it in DESCRIPTORS }.toSet()
    }

    private fun words(text: String): Sequence<String> =
        WORD_REGEX
            .findAll(text.replace(URL_REGEX, " ").lowercase())
            .map { stem(it.value) }
            .filter { it.length >= MIN_STEM_LENGTH }

    private fun wordsMatch(
        keyword: String,
        stepWord: String,
    ): Boolean {
        if (keyword == stepWord) return true
        val (shorter, longer) = if (keyword.length <= stepWord.length) keyword to stepWord else stepWord to keyword
        return shorter.length >= MIN_AFFIX_MATCH_LENGTH &&
            (longer.startsWith(shorter) || longer.endsWith(shorter))
    }

    /**
     * Strips one plural or inflection ending. A one-letter ending must leave at least three
     * letters, so short words keep their identity ("ein" must not become "ei", the stem of "Eier").
     */
    private fun stem(word: String): String {
        val lowercase = word.lowercase()
        val suffix =
            SUFFIXES.firstOrNull { suffix ->
                val minRemaining = if (suffix.length == 1) MIN_STEM_LENGTH + 1 else MIN_STEM_LENGTH
                lowercase.endsWith(suffix) && lowercase.length - suffix.length >= minRemaining
            }
        return if (suffix == null) lowercase else lowercase.dropLast(suffix.length)
    }
}
