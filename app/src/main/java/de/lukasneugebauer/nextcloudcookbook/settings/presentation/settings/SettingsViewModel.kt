package de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearAllStoresUseCase
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.settings.domain.state.SettingsScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val apiProvider: ApiProvider,
        private val clearAllStoresUseCase: ClearAllStoresUseCase,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        init {
            _uiState.update {
                SettingsScreenState.Loaded(
                    isStayAwake = preferencesManager.getStayAwake(),
                )
            }
        }

        fun setStayAwake(isStayAwake: Boolean) {
            preferencesManager.setStayAwake(isStayAwake = isStayAwake)
            _uiState.update {
                SettingsScreenState.Loaded(
                    isStayAwake = isStayAwake,
                )
            }
        }

        fun logout(callback: () -> Unit) {
            viewModelScope.launch {
                apiProvider.resetApi()
                clearAllStoresUseCase.invoke()
                clearPreferencesUseCase.invoke()
                callback.invoke()
            }
        }
    }
