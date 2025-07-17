package de.lukasneugebauer.nextcloudcookbook.auth.presentation.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.ManualLoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualLoginViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val apiProvider: NcCookbookApiProvider,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        private val preferencesManager: PreferencesManager,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ManualLoginScreenState>(ManualLoginScreenState.Loaded())
        val uiState = _uiState.asStateFlow()

        init {
            observeAuthorizationStatus()
        }

        fun onUsernameChange(newUsername: String) {
            _uiState.update {
                if (it is ManualLoginScreenState.Loaded) {
                    it.copy(username = newUsername, usernameError = null, passwordError = null)
                } else {
                    it
                }
            }
        }

        fun onPasswordChange(newPassword: String) {
            _uiState.update {
                if (it is ManualLoginScreenState.Loaded) {
                    it.copy(password = newPassword, usernameError = null, passwordError = null)
                } else {
                    it
                }
            }
        }

        fun tryManualLogin() {
            val url: String? = savedStateHandle["url"]
            if (url == null) {
                _uiState.update { ManualLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_invalid_url)) }
                return
            }

            val (username, password) = _uiState.value as? ManualLoginScreenState.Loaded ?: return

            if (!isValidUsername(username)) return
            if (!isValidPassword(password)) return

            _uiState.update {
                ManualLoginScreenState.Authenticating(username = username, password = password)
            }

            val ncAccount =
                NcAccount(
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

        private fun observeAuthorizationStatus() {
            viewModelScope.launch {
                combine(
                    accountRepository.getAccount(),
                    apiProvider.apiFlow,
                ) { account, api -> Pair(account, api) }
                    .distinctUntilChanged()
                    .collect { (account, api) ->
                        when {
                            api == null -> Unit

                            account is Resource.Error -> Unit

                            account is Resource.Success -> {
                                val userMetadata = accountRepository.getUserMetadata()
                                if (userMetadata is Resource.Error) {
                                    clearPreferencesUseCase()
                                    _uiState.update {
                                        ManualLoginScreenState.Error(
                                            uiText = userMetadata.message ?: UiText.StringResource(R.string.error_unknown),
                                        )
                                    }
                                } else {
                                    _uiState.update { ManualLoginScreenState.Authenticated }
                                }
                            }
                        }
                    }
            }
        }

        private fun isValidUsername(username: String): Boolean {
            if (username.isBlank()) {
                _uiState.update {
                    if (it is ManualLoginScreenState.Loaded) {
                        it.copy(usernameError = UiText.StringResource(R.string.error_empty_username))
                    } else {
                        it
                    }
                }
                return false
            }

            return true
        }

        private fun isValidPassword(password: String): Boolean {
            if (password.isBlank()) {
                _uiState.update {
                    if (it is ManualLoginScreenState.Loaded) {
                        it.copy(passwordError = UiText.StringResource(R.string.error_empty_password))
                    } else {
                        it
                    }
                }
                return false
            }

            return true
        }
    }
