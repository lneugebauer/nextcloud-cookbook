package de.lukasneugebauer.nextcloudcookbook.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat

fun Uri.openInBrowser(context: Context) {
    val link = this

    Intent(Intent.ACTION_VIEW).also { intent ->
        intent.data = link
        ContextCompat.startActivity(context, intent, null)
    }
}