package de.lukasneugebauer.nextcloudcookbook.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import timber.log.Timber

/**
 * Opens [this] in a Custom Tab and reports whether a browser took it.
 *
 * There is deliberately no [android.content.Intent.resolveActivity] pre-check: package visibility
 * would make it return `null` without a `<queries>` declaration, so it would report "no browser" on
 * every modern device. Implicit `ACTION_VIEW` launches themselves are not filtered.
 *
 * Pass the Activity context so the tab joins the app's own task.
 */
fun Uri.openInCustomTab(context: Context): Boolean =
    try {
        CustomTabsIntent
            .Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, this)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No browser available to open the login page")
        false
    }
