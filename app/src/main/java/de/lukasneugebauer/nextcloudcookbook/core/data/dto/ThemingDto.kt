package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Theming

data class ThemingDto(
    val name: String,
    val url: String,
    val slogan: String,
    val color: String,
    @SerializedName("color-text") val colorText: String,
    @SerializedName("color-element") val colorElement: String,
    @SerializedName("color-element-bright") val colorElementBright: String,
    @SerializedName("color-element-dark") val colorElementDark: String,
    val logo: String,
    val background: String,
    @SerializedName("background-plain") val backgroundPlain: Boolean,
    @SerializedName("background-default") val backgroundDefault: Boolean,
    val logoheader: String,
    val favicon: String
) {
    fun toTheming() = Theming(
        color = color,
        colorText = colorText,
        colorBackground = background
    )
}