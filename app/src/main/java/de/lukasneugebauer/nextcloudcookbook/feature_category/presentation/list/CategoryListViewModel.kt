package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.state.CategoryListScreenState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = mutableStateOf(CategoryListScreenState())
    val state: State<CategoryListScreenState> = _state

    init {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { categoriesResponse ->
                when (categoriesResponse) {
                    is StoreResponse.Loading -> {}
                    is StoreResponse.Data -> _state.value =
                        _state.value.copy(data = categoriesResponse.value.map { it.toCategory() })
                    is StoreResponse.NoNewData -> {}
                    is StoreResponse.Error.Exception -> {}
                    is StoreResponse.Error.Message -> {}
                }
            }
        }
    }
}
