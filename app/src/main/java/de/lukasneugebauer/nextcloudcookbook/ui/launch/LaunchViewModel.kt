package de.lukasneugebauer.nextcloudcookbook.ui.launch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    accountRepository: AccountRepository
) : ViewModel() {

    private val _state: MutableState<LaunchScreenState> = mutableStateOf(LaunchScreenState.Initial)
    val state: State<LaunchScreenState> = _state

    init {
        viewModelScope.launch {
            accountRepository.getAccount().collect {
                when (it) {
                    is Resource.Success -> _state.value =
                        LaunchScreenState.Loaded(authenticated = true)
                    is Resource.Error -> _state.value =
                        LaunchScreenState.Loaded(authenticated = false)
                }
            }
        }
    }
}

sealed class LaunchScreenState {
    object Initial : LaunchScreenState()
    data class Loaded(val authenticated: Boolean) : LaunchScreenState()
}