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
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeListScreenFlowData
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeListScreenOrder
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
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        @Suppress("ktlint:standard:property-naming")
        private val _uiState = MutableStateFlow<RecipeListScreenState>(RecipeListScreenState.Initial)
        val state = _uiState.asStateFlow()

        private val _searchAppBarState = mutableStateOf(SearchAppBarState.CLOSED)
        val searchAppBarState: State<SearchAppBarState> = _searchAppBarState

        private val _searchQueryState = MutableStateFlow("")
        val searchQueryState = _searchQueryState.asStateFlow()

        private val _selectedKeywordsState = MutableStateFlow(emptyList<String>())
        val selectedKeywordsState = _selectedKeywordsState.asStateFlow()

        @Suppress("ktlint:standard:property-naming")
        private val _orderState = MutableStateFlow(RecipeListScreenOrder.ALPHABETICAL_ASC)

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
            // Fetch all recipes to list uncategorized recipes.
            val recipePreviewsFlow =
                if (categoryName == null || categoryName == UNCATEGORIZED_RECIPE) {
                    recipeRepository.getRecipePreviewsFlow()
                } else {
                    recipeRepository.getRecipePreviewsByCategory(categoryName)
                }

            combine(
                recipePreviewsFlow,
                _searchQueryState,
                _selectedKeywordsState,
                _orderState,
            ) { recipePreviewsResponse, query, selectedKeywords, order ->
                RecipeListScreenFlowData(recipePreviewsResponse, query, selectedKeywords, order)
            }
                .onEach { (recipePreviewsResponse, query, selectedKeywords, order) ->
                    when (recipePreviewsResponse) {
                        is StoreResponse.Loading -> _uiState.update { RecipeListScreenState.Initial }
                        is StoreResponse.Data ->
                            _uiState.update {
                                val recipePreviews =
                                    recipePreviewsResponse.value
                                        .filter {
                                            // Custom filter for uncategorized recipes as they can not be directly fetch via API
                                            if (categoryName == UNCATEGORIZED_RECIPE && it.category != null) return@filter false

                                            val inFilter =
                                                selectedKeywords.isEmpty() ||
                                                    selectedKeywords.any { keyword ->
                                                        it.keywords?.contains(keyword) ?: false
                                                    }
                                            val inQuery =
                                                query.isBlank() ||
                                                    it.name.lowercase()
                                                        .contains(query.lowercase())

                                            inFilter && inQuery
                                        }
                                        .map { it.toRecipePreview() }

                                val keywords =
                                    recipePreviewsResponse.value.map { it.toRecipePreview() }
                                        .flatMap { it.keywords }.toSortedSet()

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

                        is StoreResponse.NoNewData -> Unit
                        is StoreResponse.Error -> {
                            val message =
                                recipePreviewsResponse.errorMessageOrNull()
                                    ?.let { UiText.DynamicString(it) }
                                    ?: run { UiText.StringResource(R.string.error_unknown) }
                            _uiState.update { RecipeListScreenState.Error(message) }
                        }
                    }
                }.launchIn(viewModelScope)
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
