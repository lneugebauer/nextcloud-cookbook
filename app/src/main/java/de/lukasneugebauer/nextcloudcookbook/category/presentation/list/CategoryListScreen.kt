package de.lukasneugebauer.nextcloudcookbook.category.presentation.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.RecipeListWithArgumentsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.category.domain.state.CategoryListScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import kotlin.random.Random.Default.nextInt

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.CategoryListScreen(
    navigator: DestinationsNavigator,
    viewModel: CategoryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar() },
    ) { innerPadding ->
        when (uiState) {
            is CategoryListScreenState.Initial -> Loader()
            is CategoryListScreenState.Loaded -> {
                val categories = (uiState as CategoryListScreenState.Loaded).data
                if (categories.isEmpty()) {
                    AbstractErrorScreen(uiText = UiText.StringResource(R.string.error_no_categories_found))
                } else {
                    CategoryListScreen(
                        data = categories,
                        modifier = Modifier.padding(innerPadding),
                    ) { categoryName ->
                        navigator.navigate(
                            RecipeListWithArgumentsScreenDestination(
                                categoryName = categoryName,
                                keyword = null,
                            ),
                        )
                    }
                }
            }

            is CategoryListScreenState.Error -> {
                val message = (uiState as CategoryListScreenState.Error).uiText
                AbstractErrorScreen(uiText = message)
            }
        }
    }
}

@Composable
private fun CategoryListScreen(
    data: List<Category>,
    modifier: Modifier,
    onClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(data) { index, category ->
            ListItem(
                modifier =
                    Modifier.clickable(
                        onClick = {
                            onClick.invoke(category.name)
                        },
                    ),
                trailingContent = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            text = category.recipeCount.toString(),
                        )
                    }
                },
                headlineContent = {
                    val categoryName =
                        if (category.name == "*") {
                            stringResource(R.string.recipe_uncategorised)
                        } else {
                            category.name
                        }
                    Text(text = categoryName)
                },
            )
            if (index != data.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
                )
            }
        }
    }
}

@Composable
private fun TopAppBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.common_categories)) },
    )
}

@Preview
@Composable
private fun CategoryListScreenPreview() {
    val categories =
        MutableList(10) {
            Category(name = "Category $it", nextInt(0, 20))
        }
    NextcloudCookbookTheme {
        CategoryListScreen(data = categories, modifier = Modifier, onClick = {})
    }
}
