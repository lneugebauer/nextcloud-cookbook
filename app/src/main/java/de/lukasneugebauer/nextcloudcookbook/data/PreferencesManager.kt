package de.lukasneugebauer.nextcloudcookbook.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class NcAccount(
    val name: String,
    val username: String,
    val token: String,
    val url: String
)

data class Preferences(
    val ncAccount: NcAccount,
    val useSingleSignOn: Boolean
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val NC_NAME = stringPreferencesKey("nc_name")
        val NC_USERNAME = stringPreferencesKey("nc_username")
        val NC_TOKEN = stringPreferencesKey("nc_token")
        val NC_URL = stringPreferencesKey("nc_url")
        val USE_SINGLE_SIGN_ON = booleanPreferencesKey("use_single_sign_on")
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
            val ncUrl = preferences[PreferencesKeys.NC_URL] ?: ""
            val useSingleSignOn = preferences[PreferencesKeys.USE_SINGLE_SIGN_ON] ?: false

            Preferences(
                ncAccount = NcAccount(
                    name = ncName,
                    username = ncUsername,
                    token = ncToken,
                    url = ncUrl
                ),
                useSingleSignOn = useSingleSignOn
            )
        }

    suspend fun updateNextcloudAccount(ncAccount: NcAccount) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NC_NAME] = ncAccount.name
            preferences[PreferencesKeys.NC_USERNAME] = ncAccount.username
            preferences[PreferencesKeys.NC_TOKEN] = ncAccount.token
            preferences[PreferencesKeys.NC_URL] = ncAccount.url
        }

    suspend fun updateUseSingleSignOn(useSingleSignOn: Boolean) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SINGLE_SIGN_ON] = useSingleSignOn
        }
}