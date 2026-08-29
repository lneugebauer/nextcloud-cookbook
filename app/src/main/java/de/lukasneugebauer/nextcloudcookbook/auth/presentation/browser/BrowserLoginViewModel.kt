package de.lukasneugebauer.nextcloudcookbook.auth.presentation.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.BrowserLoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
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
class BrowserLoginViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val authRepository: AuthRepository,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        private val ncCookbookApiProvider: NcCookbookApiProvider,
        private val preferencesManager: PreferencesManager,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<BrowserLoginScreenState>(BrowserLoginScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        private val initialUrl: String? = savedStateHandle["url"]

        init {
            val url = initialUrl
            if (url != null) {
                getLoginEndpoint(url = url)
                observeAuthorizationStatus()
            } else {
                _uiState.update { BrowserLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_invalid_url)) }
            }
        }

        fun onBrowserLaunched() {
            _uiState.update { state ->
                if (state is BrowserLoginScreenState.Loaded) state.copy(browserLaunched = true) else state
            }
        }

        fun onNoBrowserAvailable() {
            _uiState.update {
                BrowserLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_no_browser))
            }
        }

        /** Clearing the flag is what makes the screen's collector open the tab again. */
        fun onOpenBrowserClick() {
            _uiState.update { state ->
                if (state is BrowserLoginScreenState.Loaded) state.copy(browserLaunched = false) else state
            }
        }

        fun retry() {
            val url = initialUrl ?: return
            _uiState.update { BrowserLoginScreenState.Initial }
            getLoginEndpoint(url = url)
        }

        private fun getLoginEndpoint(url: String) {
            viewModelScope.launch {
                when (val result = authRepository.getLoginEndpoint(url)) {
                    is Resource.Success -> {
                        result.data?.loginUrl?.let { loginUrl ->
                            Timber.v("Open browser with url $loginUrl")
                            _uiState.update { BrowserLoginScreenState.Loaded(loginUrl = loginUrl) }
                            pollLoginServer(result.data.pollUrl, result.data.token)
                        } ?: run {
                            _uiState.update { BrowserLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_no_login_url)) }
                        }
                    }

                    is Resource.Error ->
                        _uiState.update {
                            BrowserLoginScreenState.Error(
                                uiText = result.message ?: UiText.StringResource(R.string.error_unknown),
                            )
                        }
                }
            }
        }

        private fun observeAuthorizationStatus() {
            viewModelScope.launch {
                combine(
                    accountRepository.getAccount(),
                    ncCookbookApiProvider.apiFlow,
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
                                        BrowserLoginScreenState.Error(
                                            uiText = userMetadata.message ?: UiText.StringResource(R.string.error_unknown),
                                        )
                                    }
                                } else {
                                    _uiState.update { BrowserLoginScreenState.Authenticated }
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
                    ncCookbookApiProvider.initApi()
                }

                is Resource.Error -> {
                    delay(POLL_DELAY)

                    if (_uiState.value is BrowserLoginScreenState.Loaded) {
                        pollLoginServer(url, token)
                    }
                }
            }
        }

        companion object {
            const val POLL_DELAY = 5_000L
        }
    }
