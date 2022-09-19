package de.lukasneugebauer.nextcloudcookbook.core.domain.model

data class SemVer2(
    val major: Int,
    val minor: Int,
    val patch: Int
) {
    fun isGreaterThanOrEqual(comparison: SemVer2): Boolean {
        if (major > comparison.major) return true

        if (major >= comparison.major &&
            minor > comparison.minor
        ) {
            return true
        }

        if (major >= comparison.major &&
            minor >= comparison.minor &&
            patch >= comparison.patch
        ) {
            return true
        }

        return false
    }
}
