package de.lukasneugebauer.nextcloudcookbook.core.util

import de.lukasneugebauer.nextcloudcookbook.BuildConfig

object Constants {
    private const val API_ENDPOINT: String = "index.php/apps/cookbook/api"
    private const val API_VERSION: String = "/v1"
    const val FULL_PATH: String = API_ENDPOINT + API_VERSION
    const val CROSSFADE_DURATION_MILLIS: Int = 750
    const val SHARED_PREFERENCES_KEY: String = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES"
}
