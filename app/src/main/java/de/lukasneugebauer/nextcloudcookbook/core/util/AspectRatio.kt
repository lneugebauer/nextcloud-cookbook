package de.lukasneugebauer.nextcloudcookbook.core.util

enum class AspectRatio(
    val ratio: Float,
) {
    PHOTO(4f / 3f),
    VIDEO(16f / 9f),
    CINEMA_SCOPE(2.4f / 1f),
}
