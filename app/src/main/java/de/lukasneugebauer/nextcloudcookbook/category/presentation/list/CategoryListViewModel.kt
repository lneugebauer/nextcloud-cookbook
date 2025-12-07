package de.lukasneugebauer.nextcloudcookbook.category.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.repository.CategoryRepository
import de.lukasneugebauer.nextcloudcookbook.category.domain.state.CategoryListScreenState
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText.DynamicString
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText.StringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.mobilenativefoundation.store.store5.StoreReadResponse
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
                .onEach { categoriesResponse ->
                    when (categoriesResponse) {
                        is StoreReadResponse.Loading -> _uiState.update { CategoryListScreenState.Initial }
                        is StoreReadResponse.Data ->
                            _uiState.update {
                                CategoryListScreenState.Loaded(
                                    categoriesResponse.value
                                        .filter { it.recipeCount > 0 }
                                        .map { it.toCategory() },
                                )
                            }

                        is StoreReadResponse.NoNewData -> Unit

                        is StoreReadResponse.Error -> {
                            val message =
                                categoriesResponse
                                    .errorMessageOrNull()
                                    ?.let { DynamicString(it) }
                                    ?: run { StringResource(R.string.error_unknown) }
                            _uiState.update { CategoryListScreenState.Error(message) }
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }
