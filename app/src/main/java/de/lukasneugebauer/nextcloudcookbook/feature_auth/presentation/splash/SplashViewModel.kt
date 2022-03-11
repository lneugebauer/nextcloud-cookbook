package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.SplashScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    accountRepository: AccountRepository,
    apiProvider: ApiProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashScreenState>(SplashScreenState.Initial)
    val uiState: StateFlow<SplashScreenState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccount(),
                apiProvider.ncCookbookApiFlow
            ) { account, ncCookbookApi ->
                Pair(account, ncCookbookApi)
            }.collect { (account, ncCookbookApi) ->
                when {
                    account is Resource.Success && ncCookbookApi != null -> {
                        _uiState.update { SplashScreenState.Authorized }
                    }
                    account is Resource.Error -> {
                        _uiState.update { SplashScreenState.Unauthorized }
                    }
                }
            }
        }
    }
}
