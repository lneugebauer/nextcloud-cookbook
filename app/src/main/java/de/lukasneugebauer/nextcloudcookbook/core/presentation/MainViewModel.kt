package de.lukasneugebauer.nextcloudcookbook.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AuthState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.SplashState
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.Credentials as Okhttp3Credentials

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Initial)
    val splashState: StateFlow<SplashState> = _splashState

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthorized)
    val authState: StateFlow<AuthState> = _authState

    init {
        getLoginCredentials()
    }

    fun finishSplash() {
        _splashState.update { SplashState.Loaded }
    }

    private fun getLoginCredentials() {
        viewModelScope.launch {
            accountRepository.getAccount().collect { accountResource ->
                when {
                    accountResource is Resource.Success && accountResource.data != null -> {
                        _authState.update {
                            AuthState.Authorized(
                                credentials = Credentials(
                                    baseUrl = accountResource.data.url,
                                    basic = Okhttp3Credentials.basic(
                                        username = accountResource.data.username,
                                        password = accountResource.data.token,
                                    ),
                                ),
                            )
                        }
                    }
                    else -> {
                        _authState.update { AuthState.Unauthorized }
                    }
                }
                finishSplash()
            }
        }
    }
}
