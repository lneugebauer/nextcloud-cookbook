package de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image

import androidx.compose.runtime.MutableState
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
class AuthorizedImageViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state: MutableState<AuthorizedImageState> = mutableStateOf(AuthorizedImageState())
    val state: State<AuthorizedImageState> = _state

    init {
        viewModelScope.launch {
            accountRepository.getAccount().collect {
                when (it) {
                    is Resource.Success -> _state.value = _state.value.copy(account = it.data)
                    is Resource.Error -> _state.value = _state.value.copy(error = it.text)
                }
            }
        }
    }
}
