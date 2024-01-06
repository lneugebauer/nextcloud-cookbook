package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName

data class CapabilitiesOcsDataDto(
    @SerializedName("version")
    val version: NextcloudVersionDto,
    @SerializedName("capabilities")
    val capabilities: CapabilitiesDto,
)
