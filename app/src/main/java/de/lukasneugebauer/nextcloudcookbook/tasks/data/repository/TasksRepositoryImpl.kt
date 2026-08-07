package de.lukasneugebauer.nextcloudcookbook.tasks.data.repository

import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.util.OkHttpClientProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import de.lukasneugebauer.nextcloudcookbook.tasks.data.remote.CalDavXmlParser
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.model.TaskList
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class TasksRepositoryImpl(
    private val clientProvider: OkHttpClientProvider,
    private val preferencesManager: PreferencesManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val parser: CalDavXmlParser,
) : TasksRepository {
    override suspend fun getTaskLists(): Resource<List<TaskList>> =
        withContext(ioDispatcher) {
            try {
                val account = preferencesManager.preferencesFlow.first().ncAccount
                val baseUrl =
                    account.url.addSuffix("/").toHttpUrlOrNull()
                        ?: return@withContext Resource.Error(UiText.StringResource(R.string.error_unknown))
                val calendarsUrl =
                    baseUrl
                        .newBuilder()
                        .addPathSegments("remote.php/dav/calendars")
                        .addPathSegment(account.username)
                        .addPathSegment("")
                        .build()

                val request =
                    Request
                        .Builder()
                        .url(calendarsUrl)
                        .method("PROPFIND", PROPFIND_BODY.toRequestBody(XML_MEDIA_TYPE))
                        .header("Authorization", basicAuth(account))
                        .header("Depth", "1")
                        .build()

                clientProvider.getCurrentClient().newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (!response.isSuccessful || body == null) {
                        Timber.e("PROPFIND for task lists failed with code ${response.code}")
                        return@withContext Resource.Error(UiText.StringResource(R.string.settings_shopping_list_error))
                    }

                    val taskLists =
                        parser.parseTaskLists(body).mapNotNull { calendar ->
                            baseUrl
                                .resolve(calendar.href)
                                ?.toString()
                                ?.let { TaskList(url = it, displayName = calendar.displayName) }
                        }
                    Resource.Success(taskLists)
                }
            } catch (e: Exception) {
                Timber.e(e, "Loading task lists failed")
                Resource.Error(UiText.StringResource(R.string.settings_shopping_list_error))
            }
        }

    override suspend fun createTasks(
        listUrl: String,
        summaries: List<String>,
    ): Resource<Int> =
        withContext(ioDispatcher) {
            try {
                val account = preferencesManager.preferencesFlow.first().ncAccount
                val client = clientProvider.getCurrentClient()
                var created = 0

                summaries.forEach { summary ->
                    val uid = UUID.randomUUID().toString()
                    val taskUrl =
                        listUrl.addSuffix("/").toHttpUrlOrNull()?.resolve("$uid.ics")
                            ?: return@withContext Resource.Error(UiText.StringResource(R.string.shopping_list_error_adding))

                    val request =
                        Request
                            .Builder()
                            .url(taskUrl)
                            .put(buildVTodo(uid, summary).toRequestBody(CALENDAR_MEDIA_TYPE))
                            .header("Authorization", basicAuth(account))
                            .header("If-None-Match", "*")
                            .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            created++
                        } else {
                            Timber.e("Creating task failed with code ${response.code}")
                        }
                    }
                }

                if (created == summaries.size) {
                    Resource.Success(created)
                } else {
                    Resource.Error(UiText.StringResource(R.string.shopping_list_error_adding), created)
                }
            } catch (e: Exception) {
                Timber.e(e, "Creating tasks failed")
                Resource.Error(UiText.StringResource(R.string.shopping_list_error_adding))
            }
        }

    private fun basicAuth(account: NcAccount): String = Credentials.basic(account.username, account.token, UTF_8)

    private fun buildVTodo(
        uid: String,
        summary: String,
    ): String {
        val timestamp = TIMESTAMP_FORMATTER.format(Instant.now())
        val escapedSummary =
            summary
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")

        return listOf(
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "PRODID:-//Nextcloud Cookbook Android//EN",
            "BEGIN:VTODO",
            "UID:$uid",
            "CREATED:$timestamp",
            "DTSTAMP:$timestamp",
            "LAST-MODIFIED:$timestamp",
            "SUMMARY:$escapedSummary",
            "END:VTODO",
            "END:VCALENDAR",
        ).joinToString(separator = "\r\n", postfix = "\r\n")
    }

    companion object {
        private val XML_MEDIA_TYPE = "application/xml; charset=utf-8".toMediaType()
        private val CALENDAR_MEDIA_TYPE = "text/calendar; charset=utf-8".toMediaType()
        private val TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
        private val PROPFIND_BODY =
            """
            <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
              <d:prop>
                <d:resourcetype/>
                <d:displayname/>
                <c:supported-calendar-component-set/>
              </d:prop>
            </d:propfind>
            """.trimIndent()
    }
}
