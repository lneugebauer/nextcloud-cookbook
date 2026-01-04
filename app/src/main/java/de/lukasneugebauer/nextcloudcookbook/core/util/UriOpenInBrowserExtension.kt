package de.lukasneugebauer.nextcloudcookbook.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Uri.openInBrowser(context: Context) {
    val link = this

    Intent(Intent.ACTION_VIEW).also { intent ->
        intent.data = link
        context.startActivity(intent)
    }
}
