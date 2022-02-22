package de.lukasneugebauer.nextcloudcookbook.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.STAY_AWAKE_DEFAULT
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.STAY_AWAKE_KEY
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    private object PreferencesKeys {
        val NC_NAME = stringPreferencesKey("nc_name")
        val NC_USERNAME = stringPreferencesKey("nc_username")
        val NC_TOKEN = stringPreferencesKey("nc_token")
        val NC_URL = stringPreferencesKey("nc_url")
        val RECIPE_OF_THE_DAY_ID = intPreferencesKey("recipe_of_the_day_id")
        val RECIPE_OF_THE_DAY_UPDATED_AT = longPreferencesKey("recipe_of_the_day_updated_at")
    }

    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception)
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
            val recipeOfTheDayId = preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] ?: 0
            val recipeOfTheDayUpdatedAt =
                preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] ?: 0

            de.lukasneugebauer.nextcloudcookbook.core.domain.model.Preferences(
                ncAccount = NcAccount(
                    name = ncName,
                    username = ncUsername,
                    token = ncToken,
                    url = ncUrl
                ),
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

    fun getStayAwake(): Boolean {
        return sharedPreferences.getBoolean(STAY_AWAKE_KEY, STAY_AWAKE_DEFAULT)
    }

    suspend fun updateNextcloudAccount(ncAccount: NcAccount) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NC_NAME] = ncAccount.name
            preferences[PreferencesKeys.NC_USERNAME] = ncAccount.username
            preferences[PreferencesKeys.NC_TOKEN] = ncAccount.token
            preferences[PreferencesKeys.NC_URL] = ncAccount.url
        }

    suspend fun updateRecipeOfTheDay(recipeOfTheDay: RecipeOfTheDay) =
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] = recipeOfTheDay.id
            preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] =
                recipeOfTheDay.updatedAt.toEpochSecond(ZoneOffset.UTC)
        }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
        sharedPreferences.edit().clear().apply()
    }
}