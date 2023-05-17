package de.lukasneugebauer.nextcloudcookbook.category.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.category.domain.state.CategoryListScreenState
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<CategoryListScreenState>(CategoryListScreenState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        categoryRepository.getCategories().onEach { categoriesResponse ->
            when (categoriesResponse) {
                is StoreResponse.Loading -> _uiState.update { CategoryListScreenState.Initial }
                is StoreResponse.Data -> _uiState.update {
                    CategoryListScreenState.Loaded(
                        categoriesResponse.value.map { it.toCategory() },
                    )
                }
                is StoreResponse.NoNewData -> Unit
                is StoreResponse.Error -> {
                    val message = categoriesResponse.errorMessageOrNull()
                        ?.let { UiText.DynamicString(it) }
                        ?: run { UiText.StringResource(R.string.error_unknown) }
                    _uiState.update { CategoryListScreenState.Error(message) }
                }
            }
        }.launchIn(viewModelScope)
    }
}
