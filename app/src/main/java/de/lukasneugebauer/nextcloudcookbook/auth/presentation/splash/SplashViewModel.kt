package de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.SplashScreenState
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashScreenState>(SplashScreenState.Initial)
        val uiState: StateFlow<SplashScreenState> = _uiState

        fun initialize() {
            accountRepository
                .getAccount()
                .distinctUntilChanged()
                .onEach { account ->
                    if (account is Resource.Success) {
                        val userMetadata = accountRepository.getUserMetadata()

                        when {
                            userMetadata is Resource.Success -> {
                                _uiState.update { SplashScreenState.Authorized }
                            }
                            userMetadata is Resource.Error && userMetadata.isAuthError -> {
                                // Server explicitly rejected credentials (401/403)
                                _uiState.update { SplashScreenState.Unauthorized }
                            }
                            else -> {
                                // Network error but we have stored credentials - allow offline access
                                _uiState.update { SplashScreenState.Authorized }
                            }
                        }
                    } else {
                        // No stored credentials
                        _uiState.update { SplashScreenState.Unauthorized }
                    }
                }.launchIn(viewModelScope)
        }
    }
