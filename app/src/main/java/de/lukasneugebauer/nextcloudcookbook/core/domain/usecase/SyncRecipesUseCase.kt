package de.lukasneugebauer.nextcloudcookbook.core.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.dao.RecipeDao
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
                    .fresh(Unit)
                    .mapNotNull { preview -> preview.idOrNull?.let { it to preview } }
                    .toMap()
            val cached = recipeDao.getSyncStates().associate { it.id to it.syncedDateModified }

            // Recipes that vanished from the preview list no longer exist on the server.
            // clear(key) drops the in-memory entry too, which deleting the row would not.
            (cached.keys - previewsById.keys).forEach { id ->
                recipeStore.clear(id)
            }

            val outdated =
                previewsById.filter { (id, preview) ->
                    needsFetch(id = id, cached = cached, dateModified = preview.dateModified)
                }
            Timber.d("Syncing ${outdated.size} of ${previewsById.size} recipes")

            var hadFailures = false
            outdated.forEach { (id, preview) ->
                try {
                    recipeStore.fresh(id)
                    // Record what we compared against, not the fetched recipe's own dateModified.
                    recipeDao.markSynced(id = id, dateModified = preview.dateModified)
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
         * A recipe the cache has never seen always needs fetching. Otherwise it is fetched only
         * when the server reports a different `dateModified` — a server that omits the field
         * leaves us nothing to compare, and refetching on every sync is exactly what this
         * avoids, so an already cached recipe is left alone.
         */
        private fun needsFetch(
            id: String,
            cached: Map<String, String?>,
            dateModified: String?,
        ): Boolean {
            if (!cached.containsKey(id)) return true
            return dateModified != null && cached[id] != dateModified
        }

        data class Result(
            val hadFailures: Boolean,
        )
    }
