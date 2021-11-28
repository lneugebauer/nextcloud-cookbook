package de.lukasneugebauer.nextcloudcookbook.core.domain.model

import java.time.LocalDateTime

data class RecipeOfTheDay(
    val id: Int,
    val updatedAt: LocalDateTime
)