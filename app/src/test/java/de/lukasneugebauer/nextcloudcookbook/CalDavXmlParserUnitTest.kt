package de.lukasneugebauer.nextcloudcookbook

import de.lukasneugebauer.nextcloudcookbook.tasks.data.remote.CalDavXmlParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CalDavXmlParserUnitTest {
    private val parser = CalDavXmlParser()

    @Test
    fun multistatus_WithMixedCalendars_ReturnsOnlyVTodoCalendars() {
        val xml =
            """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
                <d:response>
                    <d:href>/remote.php/dav/calendars/user/</d:href>
                    <d:propstat>
                        <d:prop>
                            <d:resourcetype><d:collection/></d:resourcetype>
                        </d:prop>
                        <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                </d:response>
                <d:response>
                    <d:href>/remote.php/dav/calendars/user/personal/</d:href>
                    <d:propstat>
                        <d:prop>
                            <d:resourcetype><d:collection/><cal:calendar/></d:resourcetype>
                            <d:displayname>Personal</d:displayname>
                            <cal:supported-calendar-component-set>
                                <cal:comp name="VEVENT"/>
                            </cal:supported-calendar-component-set>
                        </d:prop>
                        <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                </d:response>
                <d:response>
                    <d:href>/remote.php/dav/calendars/user/einkaufsliste/</d:href>
                    <d:propstat>
                        <d:prop>
                            <d:resourcetype><d:collection/><cal:calendar/></d:resourcetype>
                            <d:displayname>Einkaufsliste</d:displayname>
                            <cal:supported-calendar-component-set>
                                <cal:comp name="VTODO"/>
                            </cal:supported-calendar-component-set>
                        </d:prop>
                        <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                </d:response>
            </d:multistatus>
            """.trimIndent()

        val calendars = parser.parseTaskLists(xml)

        assertEquals(1, calendars.size)
        assertEquals("/remote.php/dav/calendars/user/einkaufsliste/", calendars[0].href)
        assertEquals("Einkaufsliste", calendars[0].displayName)
    }

    @Test
    fun multistatus_WithoutDisplayName_FallsBackToHrefSegment() {
        val xml =
            """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
                <d:response>
                    <d:href>/remote.php/dav/calendars/user/tasks/</d:href>
                    <d:propstat>
                        <d:prop>
                            <d:resourcetype><d:collection/><cal:calendar/></d:resourcetype>
                            <cal:supported-calendar-component-set>
                                <cal:comp name="VTODO"/>
                            </cal:supported-calendar-component-set>
                        </d:prop>
                        <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                </d:response>
            </d:multistatus>
            """.trimIndent()

        val calendars = parser.parseTaskLists(xml)

        assertEquals(1, calendars.size)
        assertEquals("tasks", calendars[0].displayName)
    }

    @Test
    fun multistatus_WithoutCalendars_ReturnsEmptyList() {
        val xml =
            """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
                <d:response>
                    <d:href>/remote.php/dav/calendars/user/</d:href>
                    <d:propstat>
                        <d:prop>
                            <d:resourcetype><d:collection/></d:resourcetype>
                        </d:prop>
                        <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                </d:response>
            </d:multistatus>
            """.trimIndent()

        assertEquals(0, parser.parseTaskLists(xml).size)
    }
}
