package de.lukasneugebauer.nextcloudcookbook.auth.presentation.start

import android.util.Patterns.WEB_URL
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenSignInEvent
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
        private val _uiState = MutableStateFlow<StartScreenState>(StartScreenState.Loaded())
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

        fun onLoginClick(event: StartScreenSignInEvent) {
            if (isValidUrl()) {
                _uiState.update {
                    if (it is StartScreenState.Loaded) {
                        it.copy(event = event)
                    } else {
                        it
                    }
                }
            }
        }

        fun onNavigate() {
            _uiState.update {
                if (it is StartScreenState.Loaded) {
                    it.copy(event = null)
                } else {
                    it
                }
            }
        }

        private fun isValidUrl(): Boolean {
            val url = (_uiState.value as? StartScreenState.Loaded)?.url
            if (url.isNullOrBlank()) {
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
