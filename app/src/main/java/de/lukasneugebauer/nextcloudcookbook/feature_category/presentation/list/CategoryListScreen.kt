package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Badge
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListScreenDestination
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import kotlin.random.Random.Default.nextInt

@Destination
@Composable
fun CategoryListScreen(
    navigator: DestinationsNavigator,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    CategoryListScreen(
        data = state.data,
        onClick = { categoryName ->
            navigator.navigate(RecipeListScreenDestination(categoryName))
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CategoryListScreen(
    data: List<Category>,
    onClick: (String) -> Unit
) {
    Scaffold(
        topBar = { CategoryListTopBar() }
    ) {
        if (data.isEmpty()) {
            Loader()
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                itemsIndexed(data) { index, category ->
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                onClick.invoke(category.name)
                            }
                        ),
                        trailing = {
                            Badge(backgroundColor = MaterialTheme.colors.primary) {
                                Text(text = category.recipeCount.toString())
                            }
                        },
                        text = {
                            Text(text = category.name)
                        }
                    )
                    if (index != data.size - 1) {
                        Divider(
                            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryListTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.common_categories)) },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}

@Preview
@Composable
private fun CategoryListScreen() {
    val categories = MutableList(10) {
        Category(name = "Category $it", nextInt(0, 20))
    }
    NextcloudCookbookTheme {
        CategoryListScreen(data = categories, onClick = {})
    }
}
