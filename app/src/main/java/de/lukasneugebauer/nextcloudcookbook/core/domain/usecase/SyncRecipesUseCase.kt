package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.core.data.freshOrNull
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeSyncState
import kotlinx.coroutines.CancellationException
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import timber.log.Timber
import javax.inject.Inject

/**
 * Brings the local recipe cache in line with the server.
 *
 * Only the preview list is fetched unconditionally. Full recipes are fetched when they are
 * missing from the cache or when their `dateModified` no longer matches the preview's, so a
 * sync that finds nothing changed costs a single request instead of one per recipe.
 */
class SyncRecipesUseCase
    @Inject
    constructor(
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
        private val recipeDao: RecipeDao,
    ) {
        suspend operator fun invoke(): Result {
            val previewsById =
                recipePreviewsStore
                    .freshOrNull(Unit)
                    .orEmpty()
                    .mapNotNull { preview -> preview.idOrNull?.let { it to preview } }
                    .toMap()
            val cached = recipeDao.getSyncStates().associateBy { it.id }
            val now = System.currentTimeMillis()

            // Recipes that vanished from the preview list no longer exist on the server.
            // clear(key) drops the in-memory entry too, which deleting the row would not.
            (cached.keys - previewsById.keys).forEach { id ->
                recipeStore.clear(id)
            }

            val outdated =
                previewsById.filter { (id, preview) ->
                    needsFetch(cached = cached[id], dateModified = preview.dateModified, now = now)
                }
            Timber.d("Syncing ${outdated.size} of ${previewsById.size} recipes")

            var hadFailures = false
            outdated.forEach { (id, preview) ->
                try {
                    recipeStore.fresh(id)
                    // Record what we compared against, not the fetched recipe's own dateModified.
                    recipeDao.markSynced(id = id, dateModified = preview.dateModified, syncedAt = now)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    hadFailures = true
                    Timber.w(e, "Failed to sync recipe $id")
                }
            }

            return Result(hadFailures = hadFailures)
        }

        /**
         * A recipe the cache has never seen always needs fetching. Otherwise it is fetched when
         * the server reports a different `dateModified`.
         *
         * A server that omits the field leaves nothing to compare against. Refetching those on
         * every sync is exactly what this use case avoids, but never refetching them means an
         * edit made on another device would never arrive, since opening a recipe reads the cache
         * too. Such a copy is therefore refreshed once it has gone [MAX_UNVERIFIED_AGE_MS]
         * without being checked, which bounds the staleness at one request per recipe per day.
         */
        private fun needsFetch(
            cached: RecipeSyncState?,
            dateModified: String?,
            now: Long,
        ): Boolean {
            if (cached == null) return true
            if (dateModified != null) return cached.syncedDateModified != dateModified
            return cached.syncedAt == null || now - cached.syncedAt >= MAX_UNVERIFIED_AGE_MS
        }

        data class Result(
            val hadFailures: Boolean,
        )

        companion object {
            /** How long a recipe with no `dateModified` to compare may go unchecked. */
            const val MAX_UNVERIFIED_AGE_MS: Long = 24 * 60 * 60 * 1000
        }
    }
