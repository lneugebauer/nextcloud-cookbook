package de.lukasneugebauer.nextcloudcookbook.auth.data.dto

import com.google.gson.annotations.SerializedName

data class PollDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("endpoint")
    val endpoint: String
)
