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
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.LoginScreenState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val apiProvider: ApiProvider,
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
                    _state.value = _state.value.copy(authorized = true)
                } else {
                    _state.value = _state.value.copy(authorized = false)
                }
            }
        }
    }

    fun manualLogin(username: String, password: String, url: String) {
        // TODO: 07.11.21 Add plausibility check for username, password and url
        val ncAccount = NcAccount(
            name = "",
            username = username,
            token = password,
            url = url
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
}