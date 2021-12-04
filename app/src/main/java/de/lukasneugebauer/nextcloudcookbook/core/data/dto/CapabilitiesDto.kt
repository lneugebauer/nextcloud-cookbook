package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities

data class CapabilitiesDto(
    val theming: ThemingDto
) {
    fun toCapabilities(): Capabilities = Capabilities(
        theming = theming.toTheming()
    )
}