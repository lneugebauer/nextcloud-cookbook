package de.lukasneugebauer.nextcloudcookbook.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class NextcloudAccount(
    val name: String,
    val username: String,
    val token: String,
    val url: String
)

data class Preferences(
    val nextcloudAccount: NextcloudAccount
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val NC_NAME = stringPreferencesKey("nc_name")
        val NC_USERNAME = stringPreferencesKey("nc_username")
        val NC_TOKEN = stringPreferencesKey("nc_token")
        val NC_URL = stringPreferencesKey("nc_url")
    }

    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Logger.e("Error reading preferences", tr = exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val ncName = preferences[PreferencesKeys.NC_NAME] ?: ""
            val ncUsername = preferences[PreferencesKeys.NC_USERNAME] ?: ""
            val ncToken = preferences[PreferencesKeys.NC_TOKEN] ?: ""
            val ncUrl = preferences[PreferencesKeys.NC_TOKEN] ?: ""

            Preferences(
                nextcloudAccount = NextcloudAccount(
                    name = ncName,
                    username = ncUsername,
                    token = ncToken,
                    url = ncUrl
                )
            )
        }

    suspend fun updateNextcloudAccount(nextcloudAccount: NextcloudAccount) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NC_NAME] = nextcloudAccount.name
            preferences[PreferencesKeys.NC_USERNAME] = nextcloudAccount.username
            preferences[PreferencesKeys.NC_TOKEN] = nextcloudAccount.token
            preferences[PreferencesKeys.NC_URL] = nextcloudAccount.url
        }
}