package de.lukasneugebauer.nextcloudcookbook.settings.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearPreferencesUseCase: ClearPreferencesUseCase,
    val sharedPreferences: SharedPreferences
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            clearPreferencesUseCase()
        }
    }
}
