package de.lukasneugebauer.nextcloudcookbook.core.util

import de.lukasneugebauer.nextcloudcookbook.BuildConfig

object Constants {
    const val API_ENDPOINT: String = "/index.php/apps/cookbook"
    const val CROSSFADE_DURATION_MILLIS: Int = 750
    const val SHARED_PREFERENCES_KEY: String = BuildConfig.APPLICATION_ID + ".SHARED_PREFERENCES"
}
