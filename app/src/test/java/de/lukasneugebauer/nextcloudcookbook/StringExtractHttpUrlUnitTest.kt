package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.core.util.extractHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtractHttpUrlUnitTest {
    @Test
    fun string_ExtractFromBareHttpsUrl_ReturnsUrl() {
        val url = "https://example.com/recipe".extractHttpUrl()
        assertEquals("https://example.com/recipe", url)
    }

    @Test
    fun string_ExtractFromBareHttpUrl_ReturnsUrl() {
        val url = "http://cookbook.local/recipe".extractHttpUrl()
        assertEquals("http://cookbook.local/recipe", url)
    }

    @Test
    fun string_ExtractFromIpv4Host_ReturnsUrl() {
        val url = "http://192.168.1.50/recipe".extractHttpUrl()
        assertEquals("http://192.168.1.50/recipe", url)
    }

    @Test
    fun string_ExtractFromIpv4HostWithPort_ReturnsUrl() {
        val url = "http://192.168.1.50:8080/recipe".extractHttpUrl()
        assertEquals("http://192.168.1.50:8080/recipe", url)
    }

    @Test
    fun string_ExtractFromIpv4HostWithPortAndNoPath_ReturnsUrl() {
        val url = "http://192.168.1.50:8080".extractHttpUrl()
        assertEquals("http://192.168.1.50:8080", url)
    }

    @Test
    fun string_ExtractFromIpv4HostWithPortAndTrailingDot_ReturnsUrlWithoutDot() {
        val url = "Recipe on the NAS http://192.168.1.50:8080/r/42.".extractHttpUrl()
        assertEquals("http://192.168.1.50:8080/r/42", url)
    }

    @Test
    fun string_ExtractFromIpv6LiteralWithPort_ReturnsUrl() {
        val url = "https://[fd00::1]:8080/recipe".extractHttpUrl()
        assertEquals("https://[fd00::1]:8080/recipe", url)
    }

    @Test
    fun string_ExtractFromIpv6Literal_ReturnsUrlWithClosingBracket() {
        val url = "http://[fd00::1]".extractHttpUrl()
        assertEquals("http://[fd00::1]", url)
    }

    @Test
    fun string_ExtractFromTextBeforeUrl_ReturnsUrl() {
        val url = "Best Lasagna https://example.com/lasagna".extractHttpUrl()
        assertEquals("https://example.com/lasagna", url)
    }

    @Test
    fun string_ExtractFromUrlFollowedByNewline_ReturnsUrl() {
        val url = "https://example.com/a\nCheck this out".extractHttpUrl()
        assertEquals("https://example.com/a", url)
    }

    @Test
    fun string_ExtractFromTwoUrls_ReturnsFirstUrl() {
        val url = "https://a.example.com/x https://b.example.com/y".extractHttpUrl()
        assertEquals("https://a.example.com/x", url)
    }

    @Test
    fun string_ExtractFromUrlWithQuery_ReturnsUrlWithQuery() {
        val url = "https://example.com/r?portion=4&unit=g".extractHttpUrl()
        assertEquals("https://example.com/r?portion=4&unit=g", url)
    }

    @Test
    fun string_ExtractFromUrlWithTrailingDot_ReturnsUrlWithoutDot() {
        val url = "Look at https://example.com/recipe.".extractHttpUrl()
        assertEquals("https://example.com/recipe", url)
    }

    @Test
    fun string_ExtractFromUrlInParentheses_ReturnsUrlWithoutParentheses() {
        val url = "(https://example.com/recipe)".extractHttpUrl()
        assertEquals("https://example.com/recipe", url)
    }

    @Test
    fun string_ExtractFromUrlWithBalancedParentheses_ReturnsUrlWithParentheses() {
        val url = "https://en.wikipedia.org/wiki/Lasagne_(dish)".extractHttpUrl()
        assertEquals("https://en.wikipedia.org/wiki/Lasagne_(dish)", url)
    }

    @Test
    fun string_ExtractFromUppercaseUrl_ReturnsUrlUnchanged() {
        val url = "HTTPS://EXAMPLE.COM/R".extractHttpUrl()
        assertEquals("HTTPS://EXAMPLE.COM/R", url)
    }

    @Test
    fun string_ExtractFromSchemeLessUrl_ReturnsNull() {
        val url = "example.com/recipe".extractHttpUrl()
        assertEquals(null, url)
    }

    @Test
    fun string_ExtractFromSchemeLessIpv4HostWithPort_ReturnsNull() {
        val url = "192.168.1.50:8080/recipe".extractHttpUrl()
        assertEquals(null, url)
    }

    @Test
    fun string_ExtractFromTextWithoutUrl_ReturnsNull() {
        val url = "Some lovely recipe".extractHttpUrl()
        assertEquals(null, url)
    }

    @Test
    fun string_ExtractFromEmptyString_ReturnsNull() {
        val url = "".extractHttpUrl()
        assertEquals(null, url)
    }

    @Test
    fun string_ExtractFromSchemeOnly_ReturnsNull() {
        val url = "https://".extractHttpUrl()
        assertEquals(null, url)
    }
}
