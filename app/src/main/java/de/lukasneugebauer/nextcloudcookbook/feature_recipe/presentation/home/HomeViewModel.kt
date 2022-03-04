package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.HomeScreenState
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.use_case.GetHomeScreenDataUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeScreenDataUseCase: GetHomeScreenDataUseCase
) : ViewModel() {

    private val _state: MutableState<HomeScreenState> = mutableStateOf(HomeScreenState())
    val state: State<HomeScreenState> = _state

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = false, data = getHomeScreenDataUseCase())
        }
    }
}
