package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeConstants.MORE_BUTTON_THRESHOLD
import kotlinx.coroutines.FlowPreview

@FlowPreview
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
                        is HomeScreenData.Row -> {
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
                        is HomeScreenData.Single -> {
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
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.common_more)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    HomeScreenDropDownMenuItemAbout(context)
                    HomeScreenDropDownMenuItemSettings(context, navController)
                }
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@Composable
fun HomeScreenDropDownMenuItemAbout(context: Context) {
    DropdownMenuItem(onClick = {
        Toast.makeText(
            context,
            "Function currently unavailable.",
            Toast.LENGTH_SHORT
        ).show()
    }) {
        Text(text = stringResource(R.string.common_about))
    }
}

@Composable
fun HomeScreenDropDownMenuItemSettings(context: Context, navController: NavController) {
    DropdownMenuItem(onClick = {
        navController.navigate(NextcloudCookbookScreen.Settings.name)
    }) {
        Text(text = stringResource(R.string.common_settings))
    }
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