package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import com.google.gson.annotations.SerializedName

data class ImportUrlDto(
    @SerializedName("url")
    val url: String,
)
