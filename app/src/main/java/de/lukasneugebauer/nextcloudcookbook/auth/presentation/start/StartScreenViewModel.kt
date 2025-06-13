package de.lukasneugebauer.nextcloudcookbook.auth.presentation.start

import android.util.Patterns.WEB_URL
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenState
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow<StartScreenState>(StartScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        fun onUrlChange(newUrl: String) {
            _uiState.update {
                if (it is StartScreenState.Loaded) {
                    it.copy(url = newUrl, urlError = null)
                } else {
                    it
                }
            }
        }

        fun onAllowSelfSignedCertificatesChange(allowSelfSignedCertificates: Boolean) {
            _uiState.update {
                if (it is StartScreenState.Loaded) {
                    it.copy(allowSelfSignedCertificates = allowSelfSignedCertificates)
                } else {
                    it
                }
            }
        }

        fun onWebViewLoginClick() {
            val currentUrl = (_uiState.value as? StartScreenState.Loaded)?.url
            val allowSelfSignedCertificates = (_uiState.value as? StartScreenState.Loaded)?.allowSelfSignedCertificates == true
            if (currentUrl != null && isValidUrl(url = currentUrl)) {
                _uiState.update {
                    StartScreenState.WebViewLogin(url = currentUrl, allowSelfSignedCertificates = allowSelfSignedCertificates)
                }
            }
        }

        fun onManualLoginClick() {
            val currentUrl = (_uiState.value as? StartScreenState.Loaded)?.url
            val allowSelfSignedCertificates = (_uiState.value as? StartScreenState.Loaded)?.allowSelfSignedCertificates == true
            if (currentUrl != null && isValidUrl(url = currentUrl)) {
                _uiState.update {
                    StartScreenState.ManualLogin(url = currentUrl, allowSelfSignedCertificates = allowSelfSignedCertificates)
                }
            }
        }

        private fun isValidUrl(url: String): Boolean {
            if (url.isBlank()) {
                _uiState.update {
                    if (it is StartScreenState.Loaded) {
                        it.copy(urlError = UiText.StringResource(R.string.error_empty_url))
                    } else {
                        it
                    }
                }

                return false
            }

            if (!url.lowercase().startsWith("http://") && !url.lowercase().startsWith("https://")) {
                _uiState.update {
                    if (it is StartScreenState.Loaded) {
                        it.copy(urlError = UiText.StringResource(R.string.error_invalid_protocol))
                    } else {
                        it
                    }
                }

                return false
            }

            if (!WEB_URL.matcher(url).matches()) {
                _uiState.update {
                    if (it is StartScreenState.Loaded) {
                        it.copy(urlError = UiText.StringResource(R.string.error_invalid_protocol))
                    } else {
                        it
                    }
                }

                return false
            }

            return true
        }
    }
