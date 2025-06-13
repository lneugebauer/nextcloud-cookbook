package de.lukasneugebauer.nextcloudcookbook.auth.presentation.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.WebViewScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WebViewViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val apiProvider: ApiProvider,
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private var pollLoginServerIsActive = false

        private val _uiState = MutableStateFlow<WebViewScreenState>(WebViewScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        fun getLoginEndpoint(url: String) {
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
                    if (pollLoginServerIsActive) {
                        pollLoginServer(url, token)
                    }
                }
            }
        }

        companion object {
            const val POLL_DELAY = 5_000L
        }
    }
