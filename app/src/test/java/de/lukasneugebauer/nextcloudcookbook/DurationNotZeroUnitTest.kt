package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class DurationNotZeroUnitTest {
    @Test
    fun duration_NotZero_ReturnsTrue() {
        val duration = Duration.ofMillis(1000L)
        assertTrue(duration.notZero())
    }

    @Test
    fun duration_NotZero_ReturnsFalse() {
        val duration = Duration.ZERO
        assertFalse(duration.notZero())
    }
}