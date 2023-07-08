package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName

data class CapabilitiesOcsDataDto(
    @SerializedName("capabilities")
    val capabilities: CapabilitiesDto,
)
