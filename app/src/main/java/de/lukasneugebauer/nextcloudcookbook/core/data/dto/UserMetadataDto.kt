package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.UserMetadata

data class UserMetadataDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("displayname")
    val displayname: String,
) {
    fun toUserMetadata(): UserMetadata =
        UserMetadata(
            id = id,
            name = displayname,
        )
}
