package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NextcloudVersion

data class NextcloudVersionDto(
    val string: String,
) {
    fun toNextcloudVersion(): NextcloudVersion = string
}
