package de.lukasneugebauer.nextcloudcookbook.core.data.dto

data class OcsDto(
    val data: OcsDataDto
)

data class OcsDataDto(
    val capabilities: CapabilitiesDto
)