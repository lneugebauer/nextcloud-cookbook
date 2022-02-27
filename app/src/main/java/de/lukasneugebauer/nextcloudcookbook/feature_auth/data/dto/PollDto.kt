package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.dto

import com.google.gson.annotations.SerializedName

data class PollDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("endpoint")
    val endpoint: String
)