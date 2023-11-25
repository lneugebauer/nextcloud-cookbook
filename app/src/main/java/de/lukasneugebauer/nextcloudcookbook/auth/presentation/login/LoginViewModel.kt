package de.lukasneugebauer.nextcloudcookbook.auth.presentation.login

import android.util.Patterns.WEB_URL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.LoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val apiProvider: ApiProvider,
    private val clearPreferencesUseCase: ClearPreferencesUseCase,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private var pollLoginServerIsActive = false

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState: StateFlow<LoginScreenState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccount(),
                apiProvider.ncCookbookApiFlow,
            ) { account, api -> Pair(account, api) }
                .distinctUntilChanged()
                .collect { (account, api) ->
                    when {
                        api == null -> Unit

                        account is Resource.Error -> _uiState.update { it.copy(authorized = false) }

                        account is Resource.Success -> {
                            val userMetadata = accountRepository.getUserMetadata()
                            if (userMetadata is Resource.Error) {
                                clearPreferencesUseCase()
                                onHideWebView()
                                _uiState.update {
                                    it.copy(authorized = false, urlError = userMetadata.message)
                                }
                            } else {
                                _uiState.update { it.copy(authorized = true) }
                            }
                        }
                    }
                }
        }
    }

    fun getLoginEndpoint(url: String) {
        if (!isValidUrl(url)) return

        viewModelScope.launch {
            when (val result = authRepository.getLoginEndpoint(url)) {
                is Resource.Success -> {
                    result.data?.loginUrl?.let { webViewUrl ->
                        Timber.v("Open web view with url $webViewUrl")
                        _uiState.value = _uiState.value.copy(webViewUrl = webViewUrl)
                        pollLoginServerIsActive = true
                        pollLoginServer(result.data.pollUrl, result.data.token)
                    } ?: run {
                        _uiState.update { it.copy(urlError = UiText.StringResource(R.string.error_no_login_url)) }
                    }
                }

                is Resource.Error -> _uiState.update { it.copy(urlError = result.message) }
            }
        }
    }

    fun tryManualLogin(username: String, password: String, url: String) {
        if (!isValidUsername(username)) return
        if (!isValidPassword(password)) return
        if (!isValidUrl(url)) return

        val ncAccount = NcAccount(
            name = "",
            username = username,
            token = password,
            url = url.removeSuffix("/"),
        )
        viewModelScope.launch {
            preferencesManager.updateNextcloudAccount(ncAccount)
            apiProvider.initApi()
        }
    }

    fun onHideWebView() {
        pollLoginServerIsActive = false
        _uiState.update { it.copy(webViewUrl = null) }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            usernameError = null,
            passwordError = null,
            urlError = null,
        )
    }

    private suspend fun pollLoginServer(url: String, token: String) {
        when (val result = authRepository.tryLogin(url, token)) {
            is Resource.Success -> {
                preferencesManager.updateNextcloudAccount(result.data?.ncAccount!!)
                apiProvider.initApi()
            }

            is Resource.Error -> {
                delay(POLL_DELAY)
                if (pollLoginServerIsActive) {
                    pollLoginServer(url, token)
                }
            }
        }
    }

    private fun isValidUsername(username: String): Boolean {
        if (username.isBlank()) {
            _uiState.value =
                _uiState.value.copy(usernameError = UiText.StringResource(R.string.error_empty_username))
            return false
        }

        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.isBlank()) {
            _uiState.value =
                _uiState.value.copy(passwordError = UiText.StringResource(R.string.error_empty_password))
            return false
        }

        return true
    }

    private fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) {
            _uiState.value =
                _uiState.value.copy(urlError = UiText.StringResource(R.string.error_empty_url))
            return false
        }

        if (!url.startsWith("https://").toLowerCase()) {
            _uiState.value =
                _uiState.value.copy(urlError = UiText.StringResource(R.string.error_invalid_protocol))
            return false
        }

        if (!WEB_URL.matcher(url).matches()) {
            _uiState.value =
                _uiState.value.copy(urlError = UiText.StringResource(R.string.error_invalid_url))
            return false
        }

        return true
    }

    companion object {
        const val POLL_DELAY = 5_000L
    }
}
