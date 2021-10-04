package de.lukasneugebauer.nextcloudcookbook.ui.launch

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.data.PreferencesManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state: MutableState<LaunchScreenState> = mutableStateOf(LaunchScreenState.Initial)
    val state: State<LaunchScreenState> = _state

    init {
        preferencesManager.preferencesFlow.map { it.nextcloudAccount }.onEach { ncAccount ->
            if (ncAccount.username.isNotEmpty() &&
                ncAccount.token.isNotEmpty() &&
                ncAccount.url.isNotEmpty()
            ) {
                _state.value = LaunchScreenState.Loaded(true)
            } else {
                _state.value = LaunchScreenState.Loaded(false)
            }
        }.launchIn(viewModelScope)
    }
}

sealed class LaunchScreenState {
    object Initial : LaunchScreenState()
    data class Loaded(val authenticated: Boolean) : LaunchScreenState()
}