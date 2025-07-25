package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.DownloadRecipeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeCreateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.keyboardAsState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeListScreenOrder
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.SearchAppBarState
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.RecipeListScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<RecipeCreateScreenDestination, String>,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    RecipeListScreenWrapper(
        navigator = navigator,
        categoryName = null,
        resultRecipient = resultRecipient,
        viewModel = viewModel,
    )
}

@Composable
fun RecipeListScreenWrapper(
    navigator: DestinationsNavigator,
    categoryName: String?,
    resultRecipient: ResultRecipient<RecipeCreateScreenDestination, String>,
    viewModel: RecipeListViewModel,
) {
    val uiState by viewModel.state.collectAsState()
    val searchAppBarState by viewModel.searchAppBarState
    val searchQueryState by viewModel.searchQueryState.collectAsState()
    val selectedKeywordsState by viewModel.selectedKeywordsState.collectAsState()
    val orderState by viewModel.orderState.collectAsState()

    Scaffold(
        topBar = {
            when (searchAppBarState) {
                SearchAppBarState.OPEN -> {
                    SearchAppBar(
                        query = searchQueryState,
                        onQueryChange = { viewModel.updateSearchQuery(it.text) },
                        onCloseClicked = { viewModel.toggleSearchAppBarVisibility() },
                    )
                }

                SearchAppBarState.CLOSED -> {
                    TopAppBar(
                        categoryName =
                            if (categoryName == "*") {
                                stringResource(R.string.recipe_uncategorised)
                            } else {
                                categoryName
                            },
                        orderState = orderState,
                        onBackClick = { navigator.navigateUp() },
                        onImportClick = { navigator.navigate(DownloadRecipeScreenDestination) },
                        onReorder = { viewModel.updateOrder(it) },
                        onSearchClick = { viewModel.toggleSearchAppBarVisibility() },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.navigate(RecipeCreateScreenDestination)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.common_add),
                )
            }
        },
    ) { innerPadding ->
        val modifierWithInnerPadding = Modifier.padding(innerPadding)
        when (uiState) {
            is RecipeListScreenState.Initial -> {
                Loader(modifier = modifierWithInnerPadding)
            }

            is RecipeListScreenState.Loaded -> {
                val recipePreviews = (uiState as RecipeListScreenState.Loaded).recipePreviews
                val keywords = (uiState as RecipeListScreenState.Loaded).keywords
                RecipeListLayout(
                    recipePreviews = recipePreviews,
                    keywords = keywords,
                    isKeywordSelected = { keyword -> selectedKeywordsState.contains(keyword) },
                    modifier = modifierWithInnerPadding,
                    onClick = { id ->
                        navigator.navigate(RecipeDetailScreenDestination(recipeId = id))
                    },
                    onKeywordClick = { keyword -> viewModel.toggleKeyword(keyword) },
                )
            }

            is RecipeListScreenState.Error -> {
                val message = (uiState as RecipeListScreenState.Error).uiText
                AbstractErrorScreen(uiText = message, modifier = modifierWithInnerPadding)
            }
        }
    }

    resultRecipient.onNavResult { result ->
        when (result) {
            is NavResult.Canceled -> Unit
            is NavResult.Value -> {
                val recipeId = result.value
                navigator.navigate(RecipeDetailScreenDestination(recipeId))
            }
        }
    }
}

@Composable
private fun RecipeListLayout(
    recipePreviews: List<RecipePreview>,
    keywords: Set<String>,
    isKeywordSelected: (keyword: String) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    onKeywordClick: (keyword: String) -> Unit,
) {
    if (recipePreviews.isEmpty()) {
        NotFoundScreen()
    } else {
        Column(modifier = modifier) {
            if (keywords.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_m)),
                    horizontalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.padding_s)),
                ) {
                    keywords.forEach {
                        item {
                            FilterChip(
                                selected = isKeywordSelected(it),
                                onClick = { onKeywordClick.invoke(it) },
                                label = {
                                    Text(text = it)
                                },
                            )
                        }
                    }
                }
                HorizontalDivider()
            }
            LazyColumn {
                itemsIndexed(recipePreviews) { index, recipePreview ->
                    ListItem(
                        modifier =
                            Modifier.clickable {
                                onClick(recipePreview.id)
                            },
                        leadingContent = {
                            AuthorizedImage(
                                imageUrl = recipePreview.imageUrl,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(dimensionResource(id = R.dimen.common_item_width_s))
                                        .clip(MaterialTheme.shapes.medium),
                            )
                        },
                        headlineContent = {
                            Text(text = recipePreview.name)
                        },
                        supportingContent = {
                            Text(
                                text = recipePreview.keywords.joinToString(separator = ", "),
                            )
                        },
                    )
                    if (index != recipePreviews.size - 1) {
                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        dimensionResource(
                                            id = R.dimen.padding_m,
                                        ),
                                ),
                        )
                    } else {
                        Gap(size = dimensionResource(id = R.dimen.fab_offset))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAppBar(
    categoryName: String?,
    orderState: RecipeListScreenOrder,
    onBackClick: () -> Unit,
    onImportClick: () -> Unit,
    onReorder: (RecipeListScreenOrder) -> Unit,
    onSearchClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val title = categoryName ?: stringResource(id = R.string.common_recipes)

    TopAppBar(
        title = {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = {
            if (categoryName != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onImportClick) {
                Icon(
                    Icons.Outlined.CloudDownload,
                    contentDescription = stringResource(R.string.recipe_import_from_url),
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.common_share),
                )
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.recipe_change_order),
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    AscendingDropdownMenuItem(
                        text = R.string.recipe_alphabetical,
                        isSelected = orderState == RecipeListScreenOrder.ALPHABETICAL_ASC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.ALPHABETICAL_ASC)
                            expanded = false
                        },
                    )
                    DescendingDropdownMenuItem(
                        text = R.string.recipe_alphabetical,
                        isSelected = orderState == RecipeListScreenOrder.ALPHABETICAL_DESC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.ALPHABETICAL_DESC)
                            expanded = false
                        },
                    )
                    AscendingDropdownMenuItem(
                        text = R.string.recipe_created_at,
                        isSelected = orderState == RecipeListScreenOrder.CREATED_ASC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.CREATED_ASC)
                            expanded = false
                        },
                    )
                    DescendingDropdownMenuItem(
                        text = R.string.recipe_created_at,
                        isSelected = orderState == RecipeListScreenOrder.CREATED_DESC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.CREATED_DESC)
                            expanded = false
                        },
                    )
                    AscendingDropdownMenuItem(
                        text = R.string.recipe_modified_at,
                        isSelected = orderState == RecipeListScreenOrder.MODIFIED_ASC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.MODIFIED_ASC)
                            expanded = false
                        },
                    )
                    DescendingDropdownMenuItem(
                        text = R.string.recipe_modified_at,
                        isSelected = orderState == RecipeListScreenOrder.MODIFIED_DESC,
                        onClick = {
                            onReorder(RecipeListScreenOrder.MODIFIED_DESC)
                            expanded = false
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun AscendingDropdownMenuItem(
    @StringRes text: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(id = text)) },
        onClick = onClick,
        leadingIcon = {
            Icon(
                Icons.Default.ArrowDropUp,
                contentDescription = stringResource(id = R.string.common_ascending),
            )
        },
        trailingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.common_selected),
                )
            }
        },
    )
}

@Composable
private fun DescendingDropdownMenuItem(
    @StringRes text: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(id = text)) },
        onClick = onClick,
        leadingIcon = {
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = stringResource(id = R.string.common_descending),
            )
        },
        trailingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.common_selected),
                )
            }
        },
    )
}

@Composable
private fun SearchAppBar(
    query: String,
    onQueryChange: (TextFieldValue) -> Unit,
    onCloseClicked: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isKeyboardOpen by keyboardAsState()
    if (isKeyboardOpen) {
        HideBottomNavigation()
    }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppBarHeight),
    ) {
        val layoutDirection = LocalLayoutDirection.current
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = query,
                    selection = if (layoutDirection == LayoutDirection.Ltr) TextRange(query.length) else TextRange.Zero,
                ),
            )
        }
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onQueryChange.invoke(it)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = stringResource(R.string.common_search),
                    modifier = Modifier.alpha(ContentAlpha.medium),
                )
            },
            leadingIcon = {
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.alpha(ContentAlpha.medium),
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (query.isNotEmpty()) {
                            textFieldValue = TextFieldValue("")
                            onQueryChange(TextFieldValue(""))
                        } else {
                            onCloseClicked()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription =
                            if (query.isNotEmpty()) {
                                stringResource(
                                    R.string.common_clear_input,
                                )
                            } else {
                                stringResource(R.string.common_close)
                            },
                    )
                }
            },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
            keyboardActions =
                KeyboardActions(
                    onSearch = {},
                ),
            singleLine = true,
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
        )
    }
}

@PreviewScreenSizes
@Composable
private fun RecipeListPreview() {
    val recipePreviews =
        List(10) { id ->
            RecipePreview(
                id = "r_$id",
                name = "Recipe $id",
                keywords = List(nextInt(0, 5)) { "Keyword $it" }.toSet(),
                category = "",
                imageUrl = "",
                createdAt = "",
                modifiedAt = "",
            )
        }
    val allKeywords = setOf("Keyword 1", "Keyword 2", "Keyword 3")
    NextcloudCookbookTheme {
        RecipeListLayout(
            recipePreviews = recipePreviews,
            keywords = allKeywords,
            isKeywordSelected = { nextBoolean() },
            onClick = {},
            onKeywordClick = {},
        )
    }
}

@Preview
@Composable
private fun TopAppBarPreview() {
    NextcloudCookbookTheme {
        TopAppBar(
            categoryName = null,
            orderState = RecipeListScreenOrder.ALPHABETICAL_ASC,
            onBackClick = {},
            onImportClick = {},
            onReorder = {},
            onSearchClick = {},
        )
    }
}

@Preview
@Composable
private fun TopAppBarWithCategoryNamePreview() {
    NextcloudCookbookTheme {
        TopAppBar(
            categoryName = "Lorem ipsum",
            orderState = RecipeListScreenOrder.ALPHABETICAL_ASC,
            onBackClick = {},
            onImportClick = {},
            onReorder = {},
            onSearchClick = {},
        )
    }
}

@Preview
@Composable
private fun SearchAppBarPreview() {
    NextcloudCookbookTheme {
        SearchAppBar(
            query = "",
            onQueryChange = {},
            onCloseClicked = {},
        )
    }
}

private val AppBarHeight = 56.dp
