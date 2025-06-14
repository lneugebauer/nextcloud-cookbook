package de.lukasneugebauer.nextcloudcookbook.auth.presentation.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.WebViewScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val authRepository: AuthRepository,
        private val apiProvider: ApiProvider,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        private val preferencesManager: PreferencesManager,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<WebViewScreenState>(WebViewScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        init {
            val url: String? = savedStateHandle["url"]
            if (url != null) {
                getLoginEndpoint(url = url)
                observeAuthorizationStatus()
            } else {
                _uiState.update { WebViewScreenState.Error(uiText = UiText.StringResource(R.string.error_invalid_url)) }
            }
        }

        private fun getLoginEndpoint(url: String) {
            viewModelScope.launch {
                when (val result = authRepository.getLoginEndpoint(url)) {
                    is Resource.Success -> {
                        result.data?.loginUrl?.let { webViewUrl ->
                            Timber.v("Open web view with url $webViewUrl")
                            _uiState.update { WebViewScreenState.Loaded(webViewUrl = webViewUrl, pollLoginServerIsActive = true) }
                            pollLoginServer(result.data.pollUrl, result.data.token)
                        } ?: run {
                            _uiState.update { WebViewScreenState.Error(uiText = UiText.StringResource(R.string.error_no_login_url)) }
                        }
                    }

                    is Resource.Error -> _uiState.update { WebViewScreenState.Error(uiText = result.message ?: UiText.StringResource(R.string.error_unknown)) }
                }
            }
        }

        private fun observeAuthorizationStatus() {
            viewModelScope.launch {
                combine(
                    accountRepository.getAccount(),
                    apiProvider.ncCookbookApiFlow,
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
                                        WebViewScreenState.Error(uiText = userMetadata.message ?: UiText.StringResource(R.string.error_unknown))
                                    }
                                } else {
                                    _uiState.update { WebViewScreenState.Authorized }
                                }
                            }
                        }
                    }
            }
        }

        private suspend fun pollLoginServer(
            url: String,
            token: String,
        ) {
            when (val result = authRepository.tryLogin(url, token)) {
                is Resource.Success -> {
                    preferencesManager.updateNextcloudAccount(result.data?.ncAccount!!)
                    apiProvider.initApi()
                }

                is Resource.Error -> {
                    delay(POLL_DELAY)

                    if ((_uiState.value as? WebViewScreenState.Loaded)?.pollLoginServerIsActive == true) {
                        pollLoginServer(url, token)
                    }
                }
            }
        }

        companion object {
            const val POLL_DELAY = 5_000L
        }
    }
