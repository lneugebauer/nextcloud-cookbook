package de.lukasneugebauer.nextcloudcookbook.core.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.MainState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableLiveData<MainState>(MainState.Initial)
    val state: LiveData<MainState> = _state

    fun setUserIsAuthorized() {
        _state.value = MainState.Authorized
    }

    fun setUserIsUnauthorized() {
        _state.value = MainState.Unauthorized
    }
}
