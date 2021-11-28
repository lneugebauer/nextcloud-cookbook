package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.Recipe
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.*
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeConstants.MORE_BUTTON_THRESHOLD

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(topBar = { HomeTopBar(navController) }) {
        if (state.loading) {
            Loader()
        }

        if (!state.loading && state.data == null) {
            Text(text = "An error occurred.")
        }

        if (!state.loading && state.data != null && state.data.isEmpty()) {
            Text(text = "No recipes found.")
        }

        if (!state.loading && state.data != null && state.data.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = dimensionResource(id = R.dimen.padding_s),
                    bottom = dimensionResource(id = R.dimen.padding_m)
                ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s))
            ) {
                items(state.data) { data ->
                    when (data) {
                        is HomeScreenDataResult.Row -> {
                            Headline(
                                text = data.headline,
                                clickable = data.recipes.size > MORE_BUTTON_THRESHOLD
                            ) {
                                navController.navigate("${NextcloudCookbookScreen.Recipes.name}?categoryName=${data.headline}")
                            }
                            RowContainer(data = data.recipes.map {
                                RowContent(it.name, it.imageUrl) {
                                    navController.navigate("${Recipe.name}?recipeId=${it.id}")
                                }
                            })
                        }
                        is HomeScreenDataResult.Single -> {
                            Headline(
                                text = stringResource(id = data.headline),
                                clickable = false,
                                onClick = {}
                            )
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

@Composable
fun HomeTopBar(navController: NavController) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = { navController.navigate(NextcloudCookbookScreen.Settings.name) }) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.common_settings)
                )
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun SingleItem(name: String, imageUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
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
            CommonItemBody(name = name, modifier = Modifier.fillMaxWidth(), onClick = {})
        }
    }
}