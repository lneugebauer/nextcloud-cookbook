package de.lukasneugebauer.nextcloudcookbook.core.data.remote.response

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.data.dto.OcsDto
import de.lukasneugebauer.nextcloudcookbook.core.data.dto.UserMetadataDto

data class UserMetadataResponse(
    @SerializedName("ocs")
    val ocs: OcsDto<UserMetadataDto>,
)
