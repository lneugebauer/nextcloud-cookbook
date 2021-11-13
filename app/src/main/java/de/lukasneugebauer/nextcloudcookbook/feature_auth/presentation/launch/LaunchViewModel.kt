package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.launch

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    accountRepository: AccountRepository
) : ViewModel() {

    private val _state = mutableStateOf(LaunchScreenState())
    val state: State<LaunchScreenState> = _state

    init {
        viewModelScope.launch {
            accountRepository.getAccount().collect {
                when (it) {
                    is Resource.Success -> _state.value = _state.value.copy(authorized = true)
                    is Resource.Error -> _state.value = _state.value.copy(authorized = false)
                }
            }
        }
    }
}