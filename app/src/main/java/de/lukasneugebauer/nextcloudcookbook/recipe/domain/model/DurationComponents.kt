package de.lukasneugebauer.nextcloudcookbook.recipe.domain.model

data class DurationComponents(
    val hours: String = "",
    val minutes: String = "",
) {
    fun toIsoStringOrNull(): String? {
        if (hours.isBlank() && minutes.isBlank() || hours == "0" && minutes == "0") return null
        return "PT${hours.ifBlank { "0" }}H${minutes.ifBlank { "0" }}M0S"
    }
}
