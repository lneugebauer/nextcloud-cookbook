package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.SemVer2

data class ApiVersionDto(
    @SerializedName("epoch")
    val epoch: Int,
    @SerializedName("major")
    val major: Int,
    @SerializedName("minor")
    val minor: Int
) {
    fun toSemVer2() = SemVer2(
        major = epoch,
        minor = major,
        patch = minor
    )
}
