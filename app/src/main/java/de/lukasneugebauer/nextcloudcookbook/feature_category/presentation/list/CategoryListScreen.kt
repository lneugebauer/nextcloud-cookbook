package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category
import kotlin.random.Random.Default.nextInt

@Composable
fun CategoryListScreen(
    navController: NavHostController,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    CategoryListScreen(
        data = state.data,
        onClick = { name ->
            navController.navigate("${NextcloudCookbookScreen.Recipes.name}?categoryName=${name}")
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(data) { category ->
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                onClick.invoke(category.name)
                            }
                        ),
                        text = {
                            Text(text = category.name)
                        }
                    )
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
    CategoryListScreen(data = categories, onClick = {})
}