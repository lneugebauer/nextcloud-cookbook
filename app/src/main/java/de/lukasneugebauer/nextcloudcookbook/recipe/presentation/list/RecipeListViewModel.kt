package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.StoreResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.SearchAppBarState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.UNCATEGORIZED_RECIPE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeListScreenState>(RecipeListScreenState.Initial)
    val state = _uiState.asStateFlow()

    private val _searchAppBarState = mutableStateOf(SearchAppBarState.CLOSED)
    val searchAppBarState: State<SearchAppBarState> = _searchAppBarState

    private val _searchQueryState = MutableStateFlow("")
    val searchQueryState = _searchQueryState.asStateFlow()

    private val _selectedKeywordsState = MutableStateFlow(emptyList<String>())
    val selectedKeywordsState = _selectedKeywordsState.asStateFlow()

    private val categoryName: String?

    init {
        categoryName = savedStateHandle["categoryName"]
        val keyword: String? = savedStateHandle["keyword"]
        if (!keyword.isNullOrBlank()) {
            _selectedKeywordsState.update { listOf(keyword) }
        }
        getRecipePreviews()
    }

    fun toggleSearchAppBarVisibility() {
        when (_searchAppBarState.value) {
            SearchAppBarState.OPEN -> _searchAppBarState.value = SearchAppBarState.CLOSED
            SearchAppBarState.CLOSED -> _searchAppBarState.value = SearchAppBarState.OPEN
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQueryState.value = query
    }

    fun toggleKeyword(keyword: String) {
        _selectedKeywordsState.update {
            val keywords = it.toMutableList()

            if (it.contains(keyword)) {
                keywords.remove(keyword)
            } else {
                keywords.add(keyword)
            }

            keywords
        }
    }

    private fun getRecipePreviews() {
        // Fetch all recipes to list uncategorized recipes.
        val recipePreviewsFlow = if (categoryName == null || categoryName == UNCATEGORIZED_RECIPE) {
            recipeRepository.getRecipePreviews()
        } else {
            recipeRepository.getRecipePreviewsByCategory(categoryName)
        }

        combine(
            recipePreviewsFlow,
            _searchQueryState,
            _selectedKeywordsState,
        ) { recipePreviewsResponse, query, selectedKeywords ->
            Triple(recipePreviewsResponse, query, selectedKeywords)
        }
            .onEach { (recipePreviewsResponse, query, selectedKeywords) ->
                when (recipePreviewsResponse) {
                    is StoreResponse.Loading -> _uiState.update { RecipeListScreenState.Initial }
                    is StoreResponse.Data -> _uiState.update {
                        val recipePreviews = recipePreviewsResponse.value
                            .filter {
                                // Custom filter for uncategorized recipes as they can not be directly fetch via API
                                if (categoryName == UNCATEGORIZED_RECIPE && it.category != null) return@filter false

                                val inFilter =
                                    selectedKeywords.isEmpty() || selectedKeywords.any { keyword ->
                                        it.keywords?.contains(keyword) ?: false
                                    }
                                val inQuery = query.isBlank() || it.name.lowercase()
                                    .contains(query.lowercase())

                                inFilter && inQuery
                            }
                            .map { it.toRecipePreview() }

                        val keywords = recipePreviewsResponse.value.map { it.toRecipePreview() }
                            .flatMap { it.keywords }.toSortedSet()

                        RecipeListScreenState.Loaded(
                            recipePreviews = recipePreviews,
                            keywords = keywords,
                        )
                    }

                    is StoreResponse.NoNewData -> Unit
                    is StoreResponse.Error -> {
                        val message = recipePreviewsResponse.errorMessageOrNull()
                            ?.let { UiText.DynamicString(it) }
                            ?: run { UiText.StringResource(R.string.error_unknown) }
                        _uiState.update { RecipeListScreenState.Error(message) }
                    }
                }
            }.launchIn(viewModelScope)
    }
}
