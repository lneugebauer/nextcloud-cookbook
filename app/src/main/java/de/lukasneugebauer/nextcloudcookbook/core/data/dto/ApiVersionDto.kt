package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName

data class ApiVersionDto(
    @SerializedName("epoch")
    val epoch: Int,
    @SerializedName("major")
    val major: Int,
    @SerializedName("minor")
    val minor: Int,
)
