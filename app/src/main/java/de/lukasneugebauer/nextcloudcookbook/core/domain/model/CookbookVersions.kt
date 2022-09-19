package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class CookbookVersions(
    val appVersion: SemVer2,
    val apiVersion: SemVer2
)
