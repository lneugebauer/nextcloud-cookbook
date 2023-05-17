package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.recipe.util.parseAsDuration
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class StringParseAsDurationUnitTest {
    @Test
    fun string_ParseBlankString_ReturnsNull() {
        val duration = "".parseAsDuration()
        assertEquals(null, duration)
    }

    @Test
    fun string_ParseNonParsableString_ReturnsNull() {
        val duration = "gibberish".parseAsDuration()
        assertEquals(null, duration)
    }

    @Test
    fun string_ParseParsableString_ReturnsDuration() {
        val duration = "PT1H35M0S".parseAsDuration()
        assertEquals(Duration.parse("PT1H35M0S"), duration)
    }
}