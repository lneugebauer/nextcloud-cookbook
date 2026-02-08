package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Preferences
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AuthState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.SplashState
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.Credentials as Okhttp3Credentials

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val accountRepository: AccountRepository,
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _splashState = MutableStateFlow<SplashState>(SplashState.Initial)
        val splashState: StateFlow<SplashState> = _splashState

        private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthorized)
        val authState: StateFlow<AuthState> = _authState

        private val _intentState = MutableStateFlow<Intent?>(null)
        val intentState: StateFlow<Intent?> = _intentState

        val preferencesState: StateFlow<Preferences> =
            preferencesManager.preferencesFlow
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue =
                        Preferences(
                            isShowIngredientSyntaxIndicator = false,
                            ncAccount =
                                de.lukasneugebauer.nextcloudcookbook.core.domain.model
                                    .NcAccount("", "", "", ""),
                            recipeOfTheDay =
                                de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay(
                                    "",
                                    java.time.LocalDateTime.now(),
                                ),
                            allowSelfSignedCertificates = false,
                        ),
                )

        init {
            getLoginCredentials()
        }

        fun finishSplash() {
            _splashState.update { SplashState.Loaded }
        }

        fun setIntent(intent: Intent) {
            _intentState.update { intent }
        }

        private fun getLoginCredentials() {
            viewModelScope.launch {
                accountRepository.getAccount().collect { accountResource ->
                    when {
                        accountResource is Resource.Success && accountResource.data != null -> {
                            // Get capabilities and cookbook version to enrich crash report data with
                            // Nextcloud, Cookbook app and Cookbook API version metadata.
                            accountRepository.getCapabilities()
                            accountRepository.getCookbookVersion()
                            _authState.update {
                                AuthState.Authorized(
                                    credentials =
                                        Credentials(
                                            baseUrl = accountResource.data.url,
                                            basic =
                                                Okhttp3Credentials.basic(
                                                    username = accountResource.data.username,
                                                    password = accountResource.data.token,
                                                ),
                                        ),
                                )
                            }
                        }

                        else -> {
                            _authState.update { AuthState.Unauthorized }
                        }
                    }
                    finishSplash()
                }
            }
        }
    }
