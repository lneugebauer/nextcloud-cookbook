package de.lukasneugebauer.nextcloudcookbook.tasks.data.remote

import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

data class CalDavCalendar(
    val href: String,
    val displayName: String,
)

class CalDavXmlParser {
    /**
     * Parses a CalDAV PROPFIND multistatus response and returns all calendars
     * that support VTODO components, i.e. Nextcloud Tasks lists.
     */
    fun parseTaskLists(xml: String): List<CalDavCalendar> {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(xml.byteInputStream())
        val responses = document.getElementsByTagNameNS(NS_DAV, "response")

        val calendars = mutableListOf<CalDavCalendar>()
        for (i in 0 until responses.length) {
            val response = responses.item(i) as? Element ?: continue
            val href =
                response
                    .getElementsByTagNameNS(NS_DAV, "href")
                    .item(0)
                    ?.textContent
                    ?.trim()
                    ?: continue

            if (!isCalendar(response) || !supportsVTodo(response)) continue

            val displayName =
                textContents(response, NS_DAV, "displayname")
                    .firstOrNull { it.isNotBlank() }
                    ?: href.trimEnd('/').substringAfterLast('/')

            calendars.add(CalDavCalendar(href = href, displayName = displayName))
        }
        return calendars
    }

    private fun isCalendar(response: Element): Boolean {
        val resourceTypes = response.getElementsByTagNameNS(NS_DAV, "resourcetype")
        for (i in 0 until resourceTypes.length) {
            val resourceType = resourceTypes.item(i) as? Element ?: continue
            if (resourceType.getElementsByTagNameNS(NS_CALDAV, "calendar").length > 0) return true
        }
        return false
    }

    private fun supportsVTodo(response: Element): Boolean {
        val comps = response.getElementsByTagNameNS(NS_CALDAV, "comp")
        for (i in 0 until comps.length) {
            val comp = comps.item(i) as? Element ?: continue
            if (comp.getAttribute("name").equals("VTODO", ignoreCase = true)) return true
        }
        return false
    }

    private fun textContents(
        element: Element,
        namespace: String,
        localName: String,
    ): List<String> {
        val nodes = element.getElementsByTagNameNS(namespace, localName)
        return (0 until nodes.length).mapNotNull { nodes.item(it)?.textContent?.trim() }
    }

    companion object {
        private const val NS_DAV = "DAV:"
        private const val NS_CALDAV = "urn:ietf:params:xml:ns:caldav"
    }
}
