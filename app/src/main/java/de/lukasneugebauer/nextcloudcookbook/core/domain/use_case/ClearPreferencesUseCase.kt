package de.lukasneugebauer.nextcloudcookbook.core.domain.use_case

import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearPreferencesUseCase @Inject constructor(private val preferencesManager: PreferencesManager) {

    suspend operator fun invoke() {
        preferencesManager.clearPreferences()
    }
}