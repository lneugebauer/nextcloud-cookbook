package de.lukasneugebauer.nextcloudcookbook.core.data

import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * Fetches [key] and returns what the cache holds afterwards, or `null` when it holds nothing.
 *
 * Store5's own `fresh` types its result as non-null, but the readers map an empty cache to `null` so
 * that the fetcher runs at all, and that `null` is handed straight back — so on an account without a
 * single recipe the non-null type is a lie the caller then trips over. This returns the nullable
 * truth instead.
 *
 * A failed fetch still throws, exactly like `fresh`, so it is never mistaken for an empty server.
 */
suspend fun <Key : Any, Output : Any> Store<Key, Output>.freshOrNull(key: Key): Output? =
    stream(StoreReadRequest.fresh(key))
        .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
        .first()
        .also { it.throwIfError() }
        .dataOrNull()
