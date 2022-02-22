package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.use_case.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.VALID_URL_REGEX
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.LoginScreenState
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.LoginWebViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val apiProvider: ApiProvider,
    private val clearPreferencesUseCase: ClearPreferencesUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState: StateFlow<LoginScreenState> = _uiState

    private val _loginWebViewState = MutableStateFlow<LoginWebViewState>(LoginWebViewState.Gone)
    val loginWebViewState: StateFlow<LoginWebViewState> = _loginWebViewState

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccount(),
                apiProvider.ncCookbookApiFlow
            ) { accountResource, ncCookbookApi ->
                Pair(accountResource, ncCookbookApi)
            }.collect { (accountResource, ncCookbookApi) ->
                Timber.d("accountResource: $accountResource, ncCookbookApi: $ncCookbookApi")
                if (accountResource is Resource.Success && ncCookbookApi != null) {
                    when (val capabilitiesResource = accountRepository.getCapabilities()) {
                        is Resource.Success -> _uiState.value = _uiState.value.copy(authorized = true)
                        is Resource.Error -> {
                            clearPreferencesUseCase()
                            _uiState.value = _uiState.value.copy(urlError = capabilitiesResource.text)
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(authorized = false)
                }
            }
        }
    }

    fun getLoginEndpoint(url: String) {
        if (!validUrl(url)) return

        viewModelScope.launch {
            when (val result =
                authRepository.getLoginEndpoint(url)) {
                is Resource.Success -> {
                    val webViewUrl = result.data?.loginUrl!!
                    Timber.v("Open web view with url $webViewUrl")
                    _loginWebViewState.value = LoginWebViewState.Visible(url = webViewUrl)
                    pollLoginServer(result.data.pollUrl, result.data.token)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(urlError = result.text)
            }
        }
    }

    private suspend fun pollLoginServer(url: String, token: String) {
        when (val result = authRepository.tryLogin(url, token)) {
            is Resource.Success -> {
                preferencesManager.updateNextcloudAccount(result.data?.ncAccount!!)
                apiProvider.initApi()
            }
            is Resource.Error -> {
                delay(1_000L)
                pollLoginServer(url, token)
            }
        }
    }

    fun tryManualLogin(username: String, password: String, url: String) {
        if (!validUsername(username)) return
        if (!validPassword(password)) return
        if (!validUrl(url)) return

        val ncAccount = NcAccount(
            name = "",
            username = username,
            token = password,
            url = url.replace("/$", "")
        )
        viewModelScope.launch {
            preferencesManager.updateNextcloudAccount(ncAccount)
            apiProvider.initApi()
        }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            usernameError = null,
            passwordError = null,
            urlError = null
        )
    }

    private fun validUsername(username: String): Boolean {
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(usernameError = "Please enter an username")
            return false
        }

        return true
    }

    private fun validPassword(password: String): Boolean {
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Please enter a password")
            return false
        }

        return true
    }

    private fun validUrl(url: String): Boolean {
        if (url.isBlank()) {
            _uiState.value = _uiState.value.copy(urlError = "Please enter an URL")
            return false
        }

        if (!url.startsWith("https://")) {
            _uiState.value =
                _uiState.value.copy(urlError = "Invalid protocol; URL must start with https://")
            return false
        }

        if (!url.matches(VALID_URL_REGEX)) {
            _uiState.value = _uiState.value.copy(urlError = "Invalid URL")
            return false
        }

        return true
    }
}