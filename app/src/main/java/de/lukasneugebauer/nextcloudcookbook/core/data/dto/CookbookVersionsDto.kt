package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.CookbookVersions
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.SemVer2

data class CookbookVersionsDto(
    @SerializedName("cookbook_version")
    val cookbookVersion: List<Int>,
    @SerializedName("api_version")
    val apiVersion: ApiVersionDto
) {
    fun toCookbookVersions() = CookbookVersions(
        appVersion = SemVer2(
            major = cookbookVersion[0],
            minor = cookbookVersion[1],
            patch = cookbookVersion[2]
        ),
        apiVersion = apiVersion.toSemVer2()
    )
}
