package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.HomeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase.GetHomeScreenDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { HomeScreenState.Loaded(getHomeScreenDataUseCase()) }
        }
    }
}
