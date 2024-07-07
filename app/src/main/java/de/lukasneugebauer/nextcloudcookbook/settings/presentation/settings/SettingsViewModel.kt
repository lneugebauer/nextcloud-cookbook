package de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearAllStoresUseCase
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val apiProvider: ApiProvider,
        private val clearAllStoresUseCase: ClearAllStoresUseCase,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        val sharedPreferences: SharedPreferences,
    ) : ViewModel() {
        fun logout(callback: () -> Unit) {
            viewModelScope.launch {
                apiProvider.resetApi()
                clearAllStoresUseCase.invoke()
                clearPreferencesUseCase.invoke()
                callback.invoke()
            }
        }
    }
