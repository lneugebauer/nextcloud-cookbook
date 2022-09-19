package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.UserStatus

data class UserStatusDto(
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerializedName("supports_emoji")
    val supportsEmoji: Boolean
) {
    fun toUserStatus() = UserStatus(
        enabled = enabled
    )
}
