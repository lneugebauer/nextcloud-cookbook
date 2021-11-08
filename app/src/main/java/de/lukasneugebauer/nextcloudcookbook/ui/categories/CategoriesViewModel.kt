package de.lukasneugebauer.nextcloudcookbook.ui.categories

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state = mutableStateOf(CategoriesScreenState())
    val state: State<CategoriesScreenState> = _state

    init {
        viewModelScope.launch {
            when (val categoriesResult = recipeRepository.getCategories()) {
                is Resource.Success -> _state.value = _state.value.copy(
                    data = categoriesResult.data ?: emptyList()
                )
                is Resource.Error -> _state.value = _state.value.copy(error = categoriesResult.text)
            }
        }
    }
}