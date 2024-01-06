package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.CookbookVersion

data class CookbookVersionDto(
    @SerializedName("cookbook_version")
    val cookbookVersion: List<Int>,
    @SerializedName("api_version")
    val apiVersion: ApiVersionDto,
) {

    fun toCookbookVersion(): CookbookVersion = CookbookVersion(
        cookbookVersion = if (cookbookVersion.size >= 3) "${cookbookVersion[0]}.${cookbookVersion[1]}.${cookbookVersion[2]}" else "Unknown",
        apiVersion = "${apiVersion.epoch}.${apiVersion.major}.${apiVersion.minor}",
    )
}
