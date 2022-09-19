package de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.SplashScreenState
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.SemVer2
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    accountRepository: AccountRepository,
    apiProvider: ApiProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashScreenState>(SplashScreenState.Initial)
    val uiState: StateFlow<SplashScreenState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccount(),
                apiProvider.ncCookbookApiFlow
            ) { account, ncCookbookApi ->
                Pair(account, ncCookbookApi)
            }.collect { (account, ncCookbookApi) ->
                when {
                    account is Resource.Success && ncCookbookApi != null -> {
                        val userEnabled = accountRepository.getCapabilities().data?.userStatus?.enabled
                        if (userEnabled == true) {
                            val appVersion = accountRepository.getVersions().data?.appVersion
                            if (appVersion?.isGreaterThanOrEqual(MINIMUM_REQUIRED_VERSION) == true) {
                                _uiState.update { SplashScreenState.Authorized }
                            } else {
                                _uiState.update { SplashScreenState.UnsupportedAppVersion }
                            }
                        }
                    }
                    account is Resource.Error -> {
                        _uiState.update { SplashScreenState.Unauthorized }
                    }
                }
            }
        }
    }

    companion object {
        private val MINIMUM_REQUIRED_VERSION: SemVer2 = SemVer2(major = 0, minor = 9, patch = 15)
    }
}
