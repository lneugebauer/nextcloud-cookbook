package de.lukasneugebauer.nextcloudcookbook.core.data.remote.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("msg")
    val msg: String,
)
