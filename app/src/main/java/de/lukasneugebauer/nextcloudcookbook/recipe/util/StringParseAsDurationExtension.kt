package de.lukasneugebauer.nextcloudcookbook.recipe.util

import timber.log.Timber
import java.time.Duration
import java.time.format.DateTimeParseException

fun String?.parseAsDuration(): Duration? {
    if (this.isNullOrBlank()) return null

    return try {
        Duration.parse(this)
    } catch (e: DateTimeParseException) {
        Timber.e(e.fillInStackTrace(), """"$this" provided as text.""")
        null
    }
}
