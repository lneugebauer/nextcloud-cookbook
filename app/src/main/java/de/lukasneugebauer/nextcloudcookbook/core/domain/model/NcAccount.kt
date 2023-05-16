package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class NcAccount(
    val name: String,
    val username: String,
    val token: String,
    val url: String,
)
