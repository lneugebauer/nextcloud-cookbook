package de.lukasneugebauer.nextcloudcookbook.core.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
