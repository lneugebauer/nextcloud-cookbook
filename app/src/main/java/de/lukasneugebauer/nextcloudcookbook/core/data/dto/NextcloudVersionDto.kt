package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NextcloudVersion

data class NextcloudVersionDto(
    @SerializedName("string")
    val string: String,
) {
    fun toNextcloudVersion(): NextcloudVersion = string
}
