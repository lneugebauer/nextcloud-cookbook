package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import org.junit.Assert.assertEquals
import org.junit.Test

class StringAddSuffixUnitTest {
    @Test
    fun string_AddSlashAsSuffix_ReturnsString() {
        val string = "https://cloud.example.tld".addSuffix("/")
        assertEquals("https://cloud.example.tld/", string)
    }

    @Test
    fun string_AddNoSuffix_ReturnsString() {
        val string = "https://cloud.example.tld/".addSuffix("/")
        assertEquals("https://cloud.example.tld/", string)
    }
}
