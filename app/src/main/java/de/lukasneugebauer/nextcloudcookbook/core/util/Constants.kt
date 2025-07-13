package de.lukasneugebauer.nextcloudcookbook.core.util

import de.lukasneugebauer.nextcloudcookbook.BuildConfig

object Constants {
    const val API_ENDPOINT: String = "index.php/apps/cookbook/api"
    private const val API_VERSION: String = "/v1"
    const val DEFAULT_RECIPE_OF_THE_DAY_ID: String = "0"
    const val FULL_PATH: String = API_ENDPOINT + API_VERSION
    const val SHARED_PREFERENCES_KEY: String = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES"
    const val ALLOW_SELF_SIGNED_CERTIFICATES_DEFAULT: Boolean = false
}
