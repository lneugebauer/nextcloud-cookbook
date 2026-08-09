package de.lukasneugebauer.nextcloudcookbook.core.data

import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.asUiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Converts a store stream into the [DataResult] the domain layer exposes, applying [transform] to
 * turn the cached representation into domain models.
 *
 * [transform] receives a nullable value because "nothing is cached" arrives as
 * `StoreReadResponse.Data(value = null)`: the readers map an empty cache to `null` so that Store5
 * runs the fetcher at all, and the first read after a fetch is forwarded whatever its value is, so
 * an account without a single recipe or category ends up here. Callers over a collection turn that
 * into an empty result; callers over a single cached entry have nothing to show and return `null`
 * from [transform] to drop the emission instead.
 *
 * [StoreReadResponse.NoNewData] is dropped: it only reports that a fetch returned nothing new, so
 * whatever was emitted before still stands and consumers have no state to update.
 */
fun <In, Out> Flow<StoreReadResponse<In>>.asDataResult(transform: (In?) -> Out?): Flow<DataResult<Out>> =
    mapNotNull { response ->
        when (response) {
            is StoreReadResponse.Loading -> DataResult.Loading
            is StoreReadResponse.Data -> transform(response.value)?.let { DataResult.Success(it) }
            is StoreReadResponse.Error ->
                DataResult.Error(
                    response.errorMessageOrNull()?.asUiText()
                        ?: UiText.StringResource(R.string.error_unknown),
                )

            is StoreReadResponse.NoNewData -> null
        }
    }
