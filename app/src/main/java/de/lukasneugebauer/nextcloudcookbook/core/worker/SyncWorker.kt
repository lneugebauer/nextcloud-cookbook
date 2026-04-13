package de.lukasneugebauer.nextcloudcookbook.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recipePreviewsStore: RecipePreviewsStore,
    private val categoriesStore: CategoriesStore,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            recipePreviewsStore.fresh(Unit)
            categoriesStore.fresh(Unit)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker failed")
            Result.retry()
        }
    }
}