package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeListScreenFlowData
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeListScreenOrder
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.SearchAppBarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
        private val refreshAllRecipesUseCase: de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.RefreshAllRecipesUseCase,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<RecipeListScreenState>(RecipeListScreenState.Initial)
        val state: StateFlow<RecipeListScreenState>
            get() = _uiState.asStateFlow()

        private val _searchAppBarState = mutableStateOf(SearchAppBarState.CLOSED)
        val searchAppBarState: State<SearchAppBarState> = _searchAppBarState

        private val _searchQueryState = MutableStateFlow("")
        val searchQueryState = _searchQueryState.asStateFlow()

        private val _selectedKeywordsState = MutableStateFlow(emptyList<String>())
        val selectedKeywordsState = _selectedKeywordsState.asStateFlow()

        @Suppress("ktlint:standard:property-naming")
        private val _orderState = MutableStateFlow(RecipeListScreenOrder.ALPHABETICAL_ASC)
        val orderState = _orderState.asStateFlow()

        private val categoryName: String? = savedStateHandle["categoryName"]

        init {
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
            _searchQueryState.update { query }
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

        fun updateOrder(order: RecipeListScreenOrder) {
            _orderState.update { order }
        }

        private fun getRecipePreviews() {
            val recipePreviewsFlow =
                if (categoryName == null) {
                    recipeRepository.getRecipePreviewsFlow()
                } else {
                    recipeRepository.getRecipePreviewsByCategory(categoryName)
                }

            combine(
                recipePreviewsFlow,
                _searchQueryState,
                _selectedKeywordsState,
                _orderState,
            ) { recipePreviewsResult, query, selectedKeywords, order ->
                RecipeListScreenFlowData(recipePreviewsResult, query, selectedKeywords, order)
            }.onEach { (recipePreviewsResult, query, selectedKeywords, order) ->
                when (recipePreviewsResult) {
                    is DataResult.Loading -> _uiState.update { RecipeListScreenState.Initial }
                    is DataResult.Success ->
                        _uiState.update {
                            val recipePreviews =
                                recipePreviewsResult.data
                                    .filter {
                                        val inFilter =
                                            selectedKeywords.isEmpty() ||
                                                selectedKeywords.any { keyword ->
                                                    it.keywords.contains(keyword)
                                                }
                                        val inQuery =
                                            query.isBlank() ||
                                                it.name
                                                    .lowercase()
                                                    .contains(query.lowercase())

                                        inFilter && inQuery
                                    }

                            val keywords =
                                recipePreviewsResult.data
                                    .flatMap { it.keywords }
                                    .toSortedSet()

                            val sortedRecipePreviews =
                                when (order) {
                                    RecipeListScreenOrder.ALPHABETICAL_ASC -> recipePreviews
                                    RecipeListScreenOrder.ALPHABETICAL_DESC -> recipePreviews.asReversed()
                                    RecipeListScreenOrder.CREATED_ASC -> {
                                        recipePreviews.sortedBy {
                                            try {
                                                val parsed =
                                                    ZonedDateTime.parse(
                                                        it.createdAt,
                                                        DATE_TIME_FORMATTER,
                                                    )
                                                parsed.toEpochSecond()
                                            } catch (e: DateTimeParseException) {
                                                Timber.e(e)
                                                0L
                                            }
                                        }
                                    }

                                    RecipeListScreenOrder.CREATED_DESC -> {
                                        recipePreviews.sortedByDescending {
                                            try {
                                                val parsed =
                                                    ZonedDateTime.parse(
                                                        it.createdAt,
                                                        DATE_TIME_FORMATTER,
                                                    )
                                                parsed.toEpochSecond()
                                            } catch (e: DateTimeParseException) {
                                                Timber.e(e)
                                                0L
                                            }
                                        }
                                    }

                                    RecipeListScreenOrder.MODIFIED_ASC -> {
                                        recipePreviews.sortedBy {
                                            try {
                                                val parsed =
                                                    ZonedDateTime.parse(
                                                        it.modifiedAt,
                                                        DATE_TIME_FORMATTER,
                                                    )
                                                parsed.toEpochSecond()
                                            } catch (e: DateTimeParseException) {
                                                Timber.e(e)
                                                0L
                                            }
                                        }
                                    }

                                    RecipeListScreenOrder.MODIFIED_DESC -> {
                                        recipePreviews.sortedByDescending {
                                            try {
                                                val parsed =
                                                    ZonedDateTime.parse(
                                                        it.modifiedAt,
                                                        DATE_TIME_FORMATTER,
                                                    )
                                                parsed.toEpochSecond()
                                            } catch (e: DateTimeParseException) {
                                                Timber.e(e)
                                                0L
                                            }
                                        }
                                    }
                                }

                            RecipeListScreenState.Loaded(
                                recipePreviews = sortedRecipePreviews,
                                keywords = keywords,
                            )
                        }

                    is DataResult.Error -> _uiState.update { RecipeListScreenState.Error(recipePreviewsResult.message) }
                }
            }.launchIn(viewModelScope)
        }

        fun refreshRecipes() {
            viewModelScope.launch {
                try {
                    refreshAllRecipesUseCase.invoke()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        companion object {
            val DATE_TIME_FORMATTER: DateTimeFormatter =
                DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendPattern("X")
                    .optionalEnd()
                    .toFormatter()
        }
    }
