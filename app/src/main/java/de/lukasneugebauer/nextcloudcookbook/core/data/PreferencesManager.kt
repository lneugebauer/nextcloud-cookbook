package de.lukasneugebauer.nextcloudcookbook.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.DEFAULT_RECIPE_OF_THE_DAY_ID
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.STAY_AWAKE_DEFAULT
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.STAY_AWAKE_KEY
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated(
    message = "Old datastore",
    replaceWith = ReplaceWith("context.dataStore54"),
)
val Context.dataStore by preferencesDataStore(name = "app")
val Context.dataStore54 by preferencesDataStore(
    name = "app_54",
    produceMigrations = { context ->
        listOf(
            object : DataMigration<Preferences> {
                override suspend fun cleanUp() {
                    context.dataStore.edit { it.clear() }
                }

                override suspend fun migrate(currentData: Preferences): Preferences {
                    val oldData = context.dataStore.data.first().asMap()
                    val currentMutablePrefs = currentData.toMutablePreferences()

                    oldData.forEach { (key, value) ->
                        when (value) {
                            is String -> currentMutablePrefs[stringPreferencesKey(key.name)] = value
                            is Int -> {
                                if (key.name == "recipe_of_the_day_id") {
                                    currentMutablePrefs[stringPreferencesKey(key.name)] = value.toString()
                                } else {
                                    currentMutablePrefs[intPreferencesKey(key.name)] = value
                                }
                            }
                            is Long -> currentMutablePrefs[longPreferencesKey(key.name)] = value
                        }
                    }

                    return currentMutablePrefs.toPreferences()
                }

                override suspend fun shouldMigrate(currentData: Preferences) = true
            },
        )
    },
)

@Singleton
class PreferencesManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val sharedPreferences: SharedPreferences,
    ) {
        private object PreferencesKeys {
            val NC_NAME = stringPreferencesKey("nc_name")
            val NC_USERNAME = stringPreferencesKey("nc_username")
            val NC_TOKEN = stringPreferencesKey("nc_token")
            val NC_URL = stringPreferencesKey("nc_url")
            val RECIPE_OF_THE_DAY_ID = stringPreferencesKey("recipe_of_the_day_id")
            val RECIPE_OF_THE_DAY_UPDATED_AT = longPreferencesKey("recipe_of_the_day_updated_at")
            val IS_SHOW_RECIPE_SYNTAX_INDICATOR = booleanPreferencesKey("is_show_recipe_syntax_indicator")
        }

        val preferencesFlow =
            context.dataStore54.data
                .catch { exception ->
                    if (exception is IOException) {
                        Timber.e(exception)
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    val isShowRecipeSyntaxIndicator = preferences[PreferencesKeys.IS_SHOW_RECIPE_SYNTAX_INDICATOR] ?: true
                    val ncName = preferences[PreferencesKeys.NC_NAME] ?: ""
                    val ncUsername = preferences[PreferencesKeys.NC_USERNAME] ?: ""
                    val ncToken = preferences[PreferencesKeys.NC_TOKEN] ?: ""
                    val ncUrl = preferences[PreferencesKeys.NC_URL] ?: ""
                    val recipeOfTheDayId = preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] ?: DEFAULT_RECIPE_OF_THE_DAY_ID
                    val recipeOfTheDayUpdatedAt =
                        preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] ?: 0

                    de.lukasneugebauer.nextcloudcookbook.core.domain.model.Preferences(
                        isShowRecipeSyntaxIndicator = isShowRecipeSyntaxIndicator,
                        ncAccount =
                            NcAccount(
                                name = ncName,
                                username = ncUsername,
                                token = ncToken,
                                url = ncUrl,
                            ),
                        recipeOfTheDay =
                            RecipeOfTheDay(
                                id = recipeOfTheDayId,
                                updatedAt =
                                    LocalDateTime.ofInstant(
                                        Instant.ofEpochSecond(
                                            recipeOfTheDayUpdatedAt,
                                        ),
                                        ZoneOffset.UTC,
                                    ),
                            ),
                    )
                }

        fun getStayAwake(): Boolean {
            return sharedPreferences.getBoolean(STAY_AWAKE_KEY, STAY_AWAKE_DEFAULT)
        }

        fun setStayAwake(isStayAwake: Boolean) {
            sharedPreferences.edit { putBoolean(STAY_AWAKE_KEY, isStayAwake) }
        }

        suspend fun updateShowRecipeSyntaxIndicator(isShowRecipeSyntaxIndicator: Boolean) {
            context.dataStore54.edit { preferences ->
                preferences[PreferencesKeys.IS_SHOW_RECIPE_SYNTAX_INDICATOR] = isShowRecipeSyntaxIndicator
            }
        }

        suspend fun updateNextcloudAccount(ncAccount: NcAccount) =
            context.dataStore54.edit { preferences ->
                preferences[PreferencesKeys.NC_NAME] = ncAccount.name
                preferences[PreferencesKeys.NC_USERNAME] = ncAccount.username
                preferences[PreferencesKeys.NC_TOKEN] = ncAccount.token
                preferences[PreferencesKeys.NC_URL] = ncAccount.url
            }

        suspend fun updateRecipeOfTheDay(recipeOfTheDay: RecipeOfTheDay) =
            context.dataStore54.edit { preferences ->
                preferences[PreferencesKeys.RECIPE_OF_THE_DAY_ID] = recipeOfTheDay.id
                preferences[PreferencesKeys.RECIPE_OF_THE_DAY_UPDATED_AT] =
                    recipeOfTheDay.updatedAt.toEpochSecond(ZoneOffset.UTC)
            }

        suspend fun clearPreferences() {
            context.dataStore54.edit { it.clear() }
            sharedPreferences.edit { clear() }
        }
    }
