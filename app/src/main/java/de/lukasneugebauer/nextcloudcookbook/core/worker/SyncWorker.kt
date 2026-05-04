package de.lukasneugebauer.nextcloudcookbook.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val categoriesStore: CategoriesStore,
    private val recipeStore: RecipeStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val previews = recipePreviewsStore.fresh(
                Unit)
            categoriesStore.fresh(Unit)
            var hadFailures = false

            previews.forEach { previewDto ->
                val id = if (!previewDto.id.isNullOrBlank()) previewDto.id else previewDto.recipeId
                if (id != null) {
                    try {
                        recipeStore.fresh(id)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        hadFailures = true
                        Timber.w(e, "Failed to sync recipe $id")
                    }
                }
            }

            if (hadFailures) Result.retry() else Result.success()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker failed")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_recipes"

        fun buildPeriodicRequest() = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        fun buildOneTimeRequest() = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
    }

}