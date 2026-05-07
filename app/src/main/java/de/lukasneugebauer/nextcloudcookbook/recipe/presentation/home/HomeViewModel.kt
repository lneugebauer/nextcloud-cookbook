package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.domain.NetworkErrorException
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.HomeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase.GetHomeScreenDataUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Initial)
        val uiState = _uiState.asStateFlow()
        private var loadJob: Job? = null

        init {
            loadData()
        }

        fun retry() {
            _uiState.update { HomeScreenState.Initial }
            loadData()
        }

        private fun loadData() {
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    try {
                        val data = getHomeScreenDataUseCase()
                        _uiState.update { HomeScreenState.Loaded(data) }
                    } catch (e: NetworkErrorException) {
                        Timber.e(e.stackTraceToString())
                        _uiState.update { HomeScreenState.ServerUnreachable }
                    } catch (e: Exception) {
                        Timber.e(e.stackTraceToString())
                        _uiState.update {
                            HomeScreenState.Error(UiText.StringResource(R.string.error_unknown))
                        }
                    }
                }
        }
    }
