package de.lukasneugebauer.nextcloudcookbook.core.util

private val HTTP_URL_REGEX = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

private const val TRAILING_PUNCTUATION = ".,;:!?>\"'"

private val CLOSING_BRACKETS = mapOf(')' to '(', ']' to '[', '}' to '{')

fun String.extractHttpUrl(): String? {
    var url = HTTP_URL_REGEX.find(this)?.value ?: return null

    while (url.isNotEmpty()) {
        val lastChar = url.last()
        val openingBracket = CLOSING_BRACKETS[lastChar]
        val isUnbalancedBracket =
            openingBracket != null &&
                url.count { it == lastChar } > url.count { it == openingBracket }
        if (lastChar !in TRAILING_PUNCTUATION && !isUnbalancedBracket) {
            break
        }
        url = url.dropLast(1)
    }

    return url.takeIf { it.substringAfter("://").isNotEmpty() }
}
