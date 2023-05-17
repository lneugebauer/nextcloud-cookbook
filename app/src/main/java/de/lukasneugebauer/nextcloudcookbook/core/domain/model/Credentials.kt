package de.lukasneugebauer.nextcloudcookbook.core.domain.model

import androidx.compose.runtime.compositionLocalOf

data class Credentials(
    val baseUrl: String,
    val basic: String,
)

val LocalCredentials = compositionLocalOf<Credentials?> { null }
