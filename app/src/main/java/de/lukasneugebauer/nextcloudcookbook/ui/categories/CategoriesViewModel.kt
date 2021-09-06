package de.lukasneugebauer.nextcloudcookbook.ui.categories

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.data.models.category.Category
import de.lukasneugebauer.nextcloudcookbook.data.repositories.RecipeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _state: MutableState<CategoriesScreenState> =
        mutableStateOf(CategoriesScreenState(data = emptyList()))
    val state: State<CategoriesScreenState> = _state

    init {
        viewModelScope.launch {
            _state.value = CategoriesScreenState(data = recipeRepository.getCategories())
        }
    }
}

data class CategoriesScreenState(
    val data: List<Category>
)