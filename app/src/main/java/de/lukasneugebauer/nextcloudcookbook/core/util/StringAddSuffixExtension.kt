package de.lukasneugebauer.nextcloudcookbook.core.util

fun String.addSuffix(
    suffix: String,
    ignoreEmpty: Boolean = true,
): String {
    if ((this.isEmpty() && ignoreEmpty) || this.endsWith(suffix)) {
        return this
    }
    return "$this$suffix"
}
