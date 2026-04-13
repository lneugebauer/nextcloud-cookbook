package de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.SplashScreenState
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.worker.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashScreenState>(SplashScreenState.Initial)
        val uiState: StateFlow<SplashScreenState> = _uiState

        fun initialize() {
            accountRepository
                .getAccount()
                .distinctUntilChanged()
                .onEach { account ->
                    if (account is Resource.Success) {
                        triggerInitialSync();
                        _uiState.update { SplashScreenState.Authorized }
                    } else {
                        _uiState.update { SplashScreenState.Unauthorized }
                    }
                }.launchIn(viewModelScope)
        }

        private fun triggerInitialSync() {
            viewModelScope.launch {
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "sync_initial",
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SyncWorker>()
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .build()
                    )
            }
        }

    }
