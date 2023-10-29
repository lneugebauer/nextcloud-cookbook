package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

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
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.FilterChip
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.keyboardAsState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeCreateScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeDetailScreenDestination
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeListScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.SearchAppBarState
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

@Destination
@Composable
fun RecipeListScreen(
    navigator: DestinationsNavigator,
    categoryName: String?,
    @Suppress("UNUSED_PARAMETER") keyword: String?,
    resultRecipient: ResultRecipient<RecipeCreateScreenDestination, Int>,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsState()
    val searchAppBarState by viewModel.searchAppBarState
    val searchQueryState by viewModel.searchQueryState.collectAsState()
    val selectedKeywordsState by viewModel.selectedKeywordsState.collectAsState()

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
                        categoryName = if (categoryName == "*") {
                            stringResource(R.string.recipe_uncategorised)
                        } else {
                            categoryName
                        },
                        onBackClick = { navigator.popBackStack() },
                        onSearchClicked = { viewModel.toggleSearchAppBarVisibility() },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navigator.navigate(RecipeCreateScreenDestination)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
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
                RecipeListScreen(
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
private fun RecipeListScreen(
    recipePreviews: List<RecipePreview>,
    keywords: Set<String>,
    isKeywordSelected: (keyword: String) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
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
                                selected = isKeywordSelected.invoke(it),
                                onClick = { onKeywordClick.invoke(it) },
                            ) {
                                Text(text = it)
                            }
                        }
                    }
                }
                Divider()
            }
            LazyColumn {
                itemsIndexed(recipePreviews) { index, recipePreview ->
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                onClick.invoke(recipePreview.id)
                            },
                        ),
                        icon = {
                            AuthorizedImage(
                                imageUrl = recipePreview.imageUrl,
                                contentDescription = recipePreview.name,
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.common_item_width_s))
                                    .clip(MaterialTheme.shapes.medium),
                            )
                        },
                        secondaryText = {
                            Text(text = recipePreview.keywords.joinToString(separator = ", "))
                        },
                        singleLineSecondaryText = false,
                        text = {
                            Text(text = recipePreview.name)
                        },
                    )
                    if (index != recipePreviews.size - 1) {
                        Divider(
                            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
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
    onBackClick: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    val title = categoryName ?: stringResource(id = R.string.common_recipes)

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = if (categoryName == null) {
            null
        } else {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.common_share),
                )
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White,
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
        modifier = Modifier
            .fillMaxWidth()
            .height(AppBarHeight),
        color = MaterialTheme.colors.primary,
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        val layoutDirection = LocalLayoutDirection.current
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = query,
                    selection = if (layoutDirection == LayoutDirection.Ltr) TextRange(query.length) else TextRange.Zero
                )
            )
        }
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onQueryChange.invoke(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    text = stringResource(R.string.common_search),
                    color = MaterialTheme.colors.onPrimary,
                )
            },
            leadingIcon = {
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.common_search),
                        modifier = Modifier.alpha(ContentAlpha.medium),
                        tint = MaterialTheme.colors.onPrimary,
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
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colors.onPrimary,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = {},
            ),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.onPrimary,
            ),
        )
    }
}

@Preview
@Composable
private fun RecipeListPreview() {
    val recipePreviews = List(10) { id ->
        RecipePreview(
            id = id,
            name = "Recipe $id",
            keywords = List(nextInt(0, 5)) { "Keyword $it" },
            category = "",
            imageUrl = "",
            createdAt = "",
            modifiedAt = "",
        )
    }
    val allKeywords = setOf("Keyword 1", "Keyword 2", "Keyword 3")
    NextcloudCookbookTheme {
        RecipeListScreen(
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
            onBackClick = {},
            onSearchClicked = {},
        )
    }
}

@Preview
@Composable
private fun TopAppBarWithCategoryNamePreview() {
    NextcloudCookbookTheme {
        TopAppBar(
            categoryName = "Lorem ipsum",
            onBackClick = {},
            onSearchClicked = {},
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
