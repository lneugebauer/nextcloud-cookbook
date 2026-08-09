package de.lukasneugebauer.nextcloudcookbook.core.util

/**
 * State of a continuously observed piece of data.
 *
 * The counterpart to [Resource], which models a single completed operation. Repositories expose
 * this instead of the caching library's own response type, so that consumers do not have to know
 * where the data came from.
 */
sealed interface DataResult<out T> {
    data object Loading : DataResult<Nothing>

    data class Success<out T>(
        val data: T,
    ) : DataResult<T>

    data class Error(
        val message: UiText,
    ) : DataResult<Nothing>
}
