package de.lukasneugebauer.nextcloudcookbook.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.data.repository.HomeScreenData
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val TAG = this.javaClass.name

    private val _state: MutableState<HomeScreenState> =
        mutableStateOf(HomeScreenState(data = emptyList()))
    val state: State<HomeScreenState> = _state

    init {
        viewModelScope.launch {
            when (val homeScreenDataResource = recipeRepository.getHomeScreenData()) {
                is Resource.Success -> {
                    _state.value = HomeScreenState(
                        data = homeScreenDataResource.data ?: emptyList()
                    )
                }
                is Resource.Error -> Logger.e("${homeScreenDataResource.text}", TAG)
            }
        }
    }
}

data class HomeScreenState(
    val data: List<HomeScreenData>
)