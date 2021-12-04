package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextcloud.android.sso.api.NextcloudAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.use_case.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.VALID_URL_REGEX
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.LoginScreenState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val apiProvider: ApiProvider,
    private val clearPreferencesUseCase: ClearPreferencesUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = mutableStateOf(LoginScreenState())
    val state: State<LoginScreenState> = _state

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccount(),
                apiProvider.ncCookbookApiFlow
            ) { accountResource, ncCookbookApi ->
                Pair(accountResource, ncCookbookApi)
            }.collect { (accountResource, ncCookbookApi) ->
                if (accountResource is Resource.Success && ncCookbookApi != null) {
                    when (val capabilitiesResource = accountRepository.getCapabilities()) {
                        is Resource.Success -> _state.value = _state.value.copy(authorized = true)
                        is Resource.Error -> {
                            clearPreferencesUseCase()
                            _state.value = _state.value.copy(urlError = capabilitiesResource.text)
                        }
                    }
                } else {
                    _state.value = _state.value.copy(authorized = false)
                }
            }
        }
    }

    fun manualLogin(username: String, password: String, url: String) {
        if (username.isBlank()) {
            _state.value = _state.value.copy(usernameError = "Please enter an username")
            return
        }

        if (password.isBlank()) {
            _state.value = _state.value.copy(passwordError = "Please enter a password")
            return
        }

        if (url.isBlank()) {
            _state.value = _state.value.copy(urlError = "Please enter an URL")
            return
        }

        if (!url.startsWith("https://")) {
            _state.value =
                _state.value.copy(urlError = "Invalid protocol; URL must start with https://")
            return
        }

        if (!url.matches(VALID_URL_REGEX)) {
            _state.value = _state.value.copy(urlError = "Invalid URL")
            return
        }

        val ncAccount = NcAccount(
            name = "",
            username = username,
            token = password,
            url = url.replace("/$", "")
        )
        viewModelScope.launch {
            preferencesManager.updateNextcloudAccount(ncAccount)
            preferencesManager.updateUseSingleSignOn(false)
            apiProvider.initApi(object : NextcloudAPI.ApiConnectedListener {
                override fun onConnected() {}
                override fun onError(ex: Exception?) {}
            })
        }
    }

    fun clearErrors() {
        _state.value = _state.value.copy(
            usernameError = null,
            passwordError = null,
            urlError = null
        )
    }
}