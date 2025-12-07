package de.lukasneugebauer.nextcloudcookbook.auth.presentation.start

import android.util.Patterns.WEB_URL
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenSignInEvent
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenState
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.ALLOW_SELF_SIGNED_CERTIFICATES_DEFAULT
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel
    @Inject
    constructor(
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<StartScreenState>(StartScreenState.Loaded())
        val uiState = _uiState.asStateFlow()

        init {
            persistAllowSelfSignedCertificates(ALLOW_SELF_SIGNED_CERTIFICATES_DEFAULT)
        }

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
            persistAllowSelfSignedCertificates(allowSelfSignedCertificates)

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

        private fun persistAllowSelfSignedCertificates(allowSelfSignedCertificates: Boolean) {
            viewModelScope.launch {
                preferencesManager.updateAllowSelfSignedCertificates(allowSelfSignedCertificates = allowSelfSignedCertificates)
            }
        }
    }
