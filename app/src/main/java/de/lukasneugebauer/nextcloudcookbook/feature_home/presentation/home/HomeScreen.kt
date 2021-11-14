package de.lukasneugebauer.nextcloudcookbook.feature_home.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.Recipe
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.*
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image.AuthorizedImage

private const val TAG = "HomeScreen"

@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(topBar = { TopBar() }) {
        if (state.data.isEmpty()) {
            Loader()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_m))
            ) {
                items(state.data) { data ->
                    when (data) {
                        is HomeScreenData.Grid -> {
                            Headline(text = stringResource(id = data.headline))
                            for (category in data.categories) {
                                CommonListItem(
                                    name = category.name,
                                    modifier = Modifier
                                        .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                        .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                                ) {
                                    navController.navigate("${NextcloudCookbookScreen.Recipes.name}?categoryName=${category.name}")
                                }
                            }
                        }
                        is HomeScreenData.Row -> {
                            Headline(text = stringResource(id = data.headline, data.categoryName))
                            RowContainer(data = data.recipes.map {
                                RowContent(it.name, it.imageUrl) {
                                    navController.navigate("${Recipe.name}?recipeId=${it.id}")
                                }
                            })
                        }
                        is HomeScreenData.Single -> {
                            Headline(text = stringResource(id = data.headline))
                            SingleItem(name = data.recipe.name, imageUrl = data.recipe.imageUrl) {
                                navController.navigate("${Recipe.name}?recipeId=${data.recipe.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun SingleItem(name: String, imageUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        onClick = onClick
    ) {
        Column {
            AuthorizedImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .fillMaxWidth()
            )
            CommonItemBody(name = name, modifier = Modifier.fillMaxWidth(), onClick = { })
        }
    }
}