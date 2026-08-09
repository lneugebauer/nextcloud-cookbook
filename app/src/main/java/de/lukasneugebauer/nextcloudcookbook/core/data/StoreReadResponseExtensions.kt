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
 * [StoreReadResponse.NoNewData] is dropped: it only reports that a fetch returned nothing new, so
 * whatever was emitted before still stands and consumers have no state to update.
 */
fun <In, Out> Flow<StoreReadResponse<In>>.asDataResult(transform: (In) -> Out): Flow<DataResult<Out>> =
    mapNotNull { response ->
        when (response) {
            is StoreReadResponse.Loading -> DataResult.Loading
            is StoreReadResponse.Data -> DataResult.Success(transform(response.value))
            is StoreReadResponse.Error ->
                DataResult.Error(
                    response.errorMessageOrNull()?.asUiText()
                        ?: UiText.StringResource(R.string.error_unknown),
                )

            is StoreReadResponse.NoNewData -> null
        }
    }
