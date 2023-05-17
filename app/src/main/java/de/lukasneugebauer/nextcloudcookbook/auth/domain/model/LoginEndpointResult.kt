package de.lukasneugebauer.nextcloudcookbook.auth.domain.model

import android.net.Uri

data class LoginEndpointResult(
    val token: String,
    val pollUrl: String,
    val loginUrl: Uri,
)
