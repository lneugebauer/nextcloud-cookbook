package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities

data class CapabilitiesDto(
    @SerializedName("theming")
    val theming: ThemingDto,
) {
    fun toCapabilities(): Capabilities =
        Capabilities(
            theming = theming.toTheming(),
        )
}
