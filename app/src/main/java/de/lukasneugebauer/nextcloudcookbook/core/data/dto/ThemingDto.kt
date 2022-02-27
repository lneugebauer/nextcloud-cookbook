package de.lukasneugebauer.nextcloudcookbook.core.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Theming

data class ThemingDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("slogan")
    val slogan: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("color-text")
    val colorText: String,
    @SerializedName("color-element")
    val colorElement: String,
    @SerializedName("color-element-bright")
    val colorElementBright: String,
    @SerializedName("color-element-dark")
    val colorElementDark: String,
    @SerializedName("logo")
    val logo: String,
    @SerializedName("background")
    val background: String,
    @SerializedName("background-plain")
    val backgroundPlain: Boolean,
    @SerializedName("background-default")
    val backgroundDefault: Boolean,
    @SerializedName("logoheader")
    val logoheader: String,
    @SerializedName("favicon")
    val favicon: String
) {
    fun toTheming() = Theming(
        color = color,
        colorText = colorText,
        colorBackground = background
    )
}