package de.lukasneugebauer.nextcloudcookbook.util

import android.util.Log
import de.lukasneugebauer.nextcloudcookbook.BuildConfig

private const val TAG: String = "NextcloudCookbook"

object Logger {
    fun v(msg: String, tag: String? = null, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tag.isNullOrBlank()) {
                Log.v(TAG, msg, tr)
            } else {
                Log.v(TAG, "$tag: $msg", tr)
            }
        }
    }

    fun d(msg: String, tag: String? = null, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tag.isNullOrBlank()) {
                Log.d(TAG, msg, tr)
            } else {
                Log.d(TAG, "$tag: $msg", tr)
            }
        }
    }

    fun i(msg: String, tag: String? = null, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (tag.isNullOrBlank()) {
                Log.i(TAG, msg, tr)
            } else {
                Log.i(TAG, "$tag: $msg", tr)
            }
        }
    }

    fun w(msg: String, tag: String? = null, tr: Throwable? = null) {
        if (tag.isNullOrBlank()) {
            Log.w(TAG, msg, tr)
        } else {
            Log.w(TAG, "$tag: $msg", tr)
        }
    }

    fun e(msg: String, tag: String? = null, tr: Throwable? = null) {
        if (tag.isNullOrBlank()) {
            Log.e(TAG, msg, tr)
        } else {
            Log.e(TAG, "$tag: $msg", tr)
        }
    }
}