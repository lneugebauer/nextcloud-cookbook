package de.lukasneugebauer.nextcloudcookbook.core.data.remote.response

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.data.dto.OcsDto

data class CapabilitiesResponse(
    @SerializedName("ocs")
    val ocs: OcsDto
)