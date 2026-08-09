package de.lukasneugebauer.nextcloudcookbook.category.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.category.domain.state.CategoryListScreenState
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel
    @Inject
    constructor(
        categoryRepository: CategoryRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<CategoryListScreenState>(CategoryListScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        init {
            categoryRepository
                .getCategories()
                .onEach { categoriesResult ->
                    when (categoriesResult) {
                        is DataResult.Loading -> _uiState.update { CategoryListScreenState.Initial }
                        is DataResult.Success ->
                            _uiState.update {
                                CategoryListScreenState.Loaded(
                                    categoriesResult.data.filter { it.recipeCount > 0 },
                                )
                            }

                        is DataResult.Error -> _uiState.update { CategoryListScreenState.Error(categoriesResult.message) }
                    }
                }.launchIn(viewModelScope)
        }
    }
