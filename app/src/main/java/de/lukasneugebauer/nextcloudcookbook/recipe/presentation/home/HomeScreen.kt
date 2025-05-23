package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.RecipeDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeListWithArgumentsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.CommonItemBody
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Headline
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.RowContainer
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.RowContent
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.UnknownErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.HomeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants.MORE_BUTTON_THRESHOLD

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.HomeScreen(
    navigator: DestinationsNavigator,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onSettingsIconClick = { navigator.navigate(SettingsScreenDestination()) },
        onHeadlineClick = { categoryName ->
            navigator.navigate(
                RecipeListWithArgumentsScreenDestination(
                    categoryName = categoryName,
                    keyword = null,
                ),
            )
        },
        onRecipeClick = { recipeId ->
            navigator.navigate(
                RecipeDetailScreenDestination(
                    recipeId = recipeId,
                ),
            )
        },
    )
}

@Composable
fun TopBar(
    onSettingsIconClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
            )
        },
        actions = {
            IconButton(onClick = onSettingsIconClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.common_settings),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeScreenState,
    onSettingsIconClick: () -> Unit,
    onHeadlineClick: (categoryName: String) -> Unit,
    onRecipeClick: (recipeId: String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                onSettingsIconClick = onSettingsIconClick,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            HomeScreenState.Initial -> Loader()
            is HomeScreenState.Loaded -> {
                val homeScreenData = uiState.data
                if (homeScreenData.isEmpty()) {
                    NotFoundScreen()
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                        contentPadding =
                            PaddingValues(
                                top = dimensionResource(id = R.dimen.padding_s),
                                bottom = dimensionResource(id = R.dimen.padding_m),
                            ),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
                    ) {
                        items(homeScreenData) { item ->
                            when (item) {
                                is HomeScreenDataResult.Row -> {
                                    Headline(
                                        text = item.headline,
                                        clickable = item.recipes.size > MORE_BUTTON_THRESHOLD,
                                        onClick = { onHeadlineClick.invoke(item.headline) },
                                    )
                                    RowContainer(
                                        data =
                                            item.recipes.map {
                                                RowContent(name = it.name, imageUrl = it.imageUrl, onClick = {
                                                    onRecipeClick.invoke(it.id)
                                                })
                                            },
                                    )
                                }

                                is HomeScreenDataResult.Single -> {
                                    Headline(
                                        text = stringResource(id = item.headline),
                                        clickable = false,
                                        onClick = {},
                                    )
                                    SingleItem(
                                        name = item.recipe.name,
                                        imageUrl = item.recipe.imageUrl,
                                        onClick = { onRecipeClick.invoke(item.recipe.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is HomeScreenState.Error -> UnknownErrorScreen()
        }
    }
}

@Composable
private fun SingleItem(
    name: String,
    imageUrl: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        onClick = onClick,
    ) {
        Column {
            AuthorizedImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier =
                    Modifier
                        .aspectRatio(16f / 9f)
                        .fillMaxWidth(),
            )
            CommonItemBody(name = name, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    NextcloudCookbookTheme {
        TopBar(
            onSettingsIconClick = {},
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        )
    }
}
