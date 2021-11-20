package de.lukasneugebauer.nextcloudcookbook.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import de.lukasneugebauer.nextcloudcookbook.core.util.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

data class NcAccount(
    val name: String,
    val username: String,
    val token: String,
    val url: String
)

data class RecipeOfTheDay(
    val id: Int,
    val updatedAt: LocalDateTime
)

data class Preferences(
    val ncAccount: NcAccount,
    val useSingleSignOn: Boolean,
    val recipeOfTheDay: RecipeOfTheDay
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val NC_NAME = stringPreferencesKey("nc_name")
        val NC_USERNAME = stringPreferencesKey("nc_username")
        val NC_TOKEN = stringPreferencesKey("nc_token")
        val NC_URL = stringPreferencesKey("nc_url")
        val USE_SINGLE_SIGN_ON = booleanPreferencesKey("use_single_sign_on")
        val RECIPE_OF_THE_DAY_ID = intPreferencesKey("recipe_of_the_day_id")
        val RECIPE_OF_THE_DAY_UPDATED_AT = longPreferencesKey("recipe_of_the_day_updated_at")
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
            val recipeOfTheDayId = preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] ?: 0
            val recipeOfTheDayUpdatedAt =
                preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] ?: 0

            Preferences(
                ncAccount = NcAccount(
                    name = ncName,
                    username = ncUsername,
                    token = ncToken,
                    url = ncUrl
                ),
                useSingleSignOn = useSingleSignOn,
                recipeOfTheDay = RecipeOfTheDay(
                    id = recipeOfTheDayId,
                    updatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(
                            recipeOfTheDayUpdatedAt
                        ), ZoneOffset.UTC
                    )
                )
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

    suspend fun updateRecipeOfTheDay(recipeOfTheDay: RecipeOfTheDay) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] = recipeOfTheDay.id
            preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] =
                recipeOfTheDay.updatedAt.toEpochSecond(ZoneOffset.UTC)
        }
}