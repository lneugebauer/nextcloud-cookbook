package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = mutableStateOf(CategoriesScreenState())
    val state: State<CategoriesScreenState> = _state

    init {
        viewModelScope.launch {
            when (val categoriesResult = categoryRepository.getCategories()) {
                is Resource.Success -> _state.value = _state.value.copy(
                    data = categoriesResult.data ?: emptyList()
                )
                is Resource.Error -> _state.value = _state.value.copy(error = categoriesResult.text)
            }
        }
    }
}