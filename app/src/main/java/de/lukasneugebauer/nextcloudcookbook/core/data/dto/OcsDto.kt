package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName

data class OcsDto(
    @SerializedName("data")
    val data: OcsDataDto
)