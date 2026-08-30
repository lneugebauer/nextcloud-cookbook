package de.lukasneugebauer.nextcloudcookbook.auth.presentation.browser

import androidx.core.net.toUri
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
import kotlinx.coroutines.Job
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
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<BrowserLoginScreenState>(BrowserLoginScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        private val initialUrl: String? = savedStateHandle[KEY_URL]

        /** Only one attempt may poll at a time; a replacement attempt cancels its predecessor. */
        private var pollJob: Job? = null

        init {
            val url = initialUrl
            val savedPollUrl: String? = savedStateHandle[KEY_POLL_URL]
            val savedPollToken: String? = savedStateHandle[KEY_POLL_TOKEN]
            val savedLoginUrl: String? = savedStateHandle[KEY_LOGIN_URL]

            when {
                url == null ->
                    _uiState.update { BrowserLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_invalid_url)) }

                savedPollUrl != null && savedPollToken != null && savedLoginUrl != null -> {
                    // The process was reclaimed while the user was signing in in the browser.
                    // Resume the token they authenticated against instead of minting a fresh one.
                    Timber.v("Resume polling saved login token")
                    _uiState.update {
                        BrowserLoginScreenState.Loaded(
                            loginUrl = savedLoginUrl.toUri(),
                            browserLaunched = savedStateHandle[KEY_BROWSER_LAUNCHED] ?: false,
                        )
                    }
                    startPolling(savedPollUrl, savedPollToken)
                    observeAuthorizationStatus()
                }

                else -> {
                    getLoginEndpoint(url = url)
                    observeAuthorizationStatus()
                }
            }
        }

        fun onBrowserLaunched() {
            savedStateHandle[KEY_BROWSER_LAUNCHED] = true
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
            savedStateHandle[KEY_BROWSER_LAUNCHED] = false
            _uiState.update { state ->
                if (state is BrowserLoginScreenState.Loaded) state.copy(browserLaunched = false) else state
            }
        }

        fun retry() {
            val url = initialUrl ?: return
            // A poll parked in its delay would otherwise wake up on the fresh `Loaded` state and
            // keep hammering the abandoned token alongside the new one.
            pollJob?.cancel()
            // And without clearing the keys the retry would resume the dead token forever.
            savedStateHandle.remove<String>(KEY_POLL_URL)
            savedStateHandle.remove<String>(KEY_POLL_TOKEN)
            savedStateHandle.remove<String>(KEY_LOGIN_URL)
            savedStateHandle.remove<Boolean>(KEY_BROWSER_LAUNCHED)
            _uiState.update { BrowserLoginScreenState.Initial }
            getLoginEndpoint(url = url)
        }

        private fun getLoginEndpoint(url: String) {
            viewModelScope.launch {
                when (val result = authRepository.getLoginEndpoint(url)) {
                    is Resource.Success -> {
                        result.data?.loginUrl?.let { loginUrl ->
                            // The URL comes straight from the server response and is handed to a
                            // browser as-is, so anything that is not http(s) never gets launched.
                            val scheme = loginUrl.scheme?.lowercase()
                            if (scheme != "http" && scheme != "https") {
                                Timber.w("Refused login url with scheme $scheme")
                                _uiState.update {
                                    BrowserLoginScreenState.Error(uiText = UiText.StringResource(R.string.error_invalid_protocol))
                                }
                                return@launch
                            }
                            Timber.v("Open browser with url $loginUrl")
                            // `Uri` is not stored as-is; keep it a `String` and `toUri()` it on read.
                            savedStateHandle[KEY_POLL_URL] = result.data.pollUrl
                            savedStateHandle[KEY_POLL_TOKEN] = result.data.token
                            savedStateHandle[KEY_LOGIN_URL] = loginUrl.toString()
                            _uiState.update { BrowserLoginScreenState.Loaded(loginUrl = loginUrl) }
                            startPolling(result.data.pollUrl, result.data.token)
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

        private fun startPolling(
            url: String,
            token: String,
        ) {
            pollJob?.cancel()
            pollJob = viewModelScope.launch { pollLoginServer(url, token) }
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

            private const val KEY_URL = "url"
            private const val KEY_POLL_URL = "pollUrl"
            private const val KEY_POLL_TOKEN = "pollToken"
            private const val KEY_LOGIN_URL = "loginUrl"
            private const val KEY_BROWSER_LAUNCHED = "browserLaunched"
        }
    }
