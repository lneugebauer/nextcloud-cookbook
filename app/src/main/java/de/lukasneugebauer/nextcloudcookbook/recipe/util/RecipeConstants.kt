package de.lukasneugebauer.nextcloudcookbook.recipe.util

import java.time.format.DateTimeFormatterBuilder

object RecipeConstants {
    const val DEFAULT_YIELD: Int = 1
    const val MORE_BUTTON_THRESHOLD: Int = 5
    const val HOME_SCREEN_CATEGORIES: Int = 3
    const val UNCATEGORIZED_RECIPE: String = "*"
    val DATE_TIME_FORMATTER =
        DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendPattern("X")
            .optionalEnd()
            .toFormatter()
}
