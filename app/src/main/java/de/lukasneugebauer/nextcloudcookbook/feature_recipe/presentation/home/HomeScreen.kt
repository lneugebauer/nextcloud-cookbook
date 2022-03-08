package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.CommonItemBody
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Headline
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.RowContainer
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.RowContent
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeDetailScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.SettingsScreenDestination
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.RecipeConstants.MORE_BUTTON_THRESHOLD

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(topBar = {
        HomeTopBar(
            onSettingsIconClick = { navigator.navigate(SettingsScreenDestination()) }
        )
    }) {
        if (state.loading) {
            Loader()
        }

        if (!state.loading && state.data != null && state.data.isEmpty()) {
            NotFoundScreen()
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
                                navigator.navigate(RecipeListScreenDestination(categoryName = data.headline))
                            }
                            RowContainer(
                                data = data.recipes.map {
                                    RowContent(it.name, it.imageUrl) {
                                        navigator.navigate(RecipeDetailScreenDestination(recipeId = it.id))
                                    }
                                }
                            )
                        }
                        is HomeScreenDataResult.Single -> {
                            Headline(
                                text = stringResource(id = data.headline),
                                clickable = false,
                                onClick = {}
                            )
                            SingleItem(name = data.recipe.name, imageUrl = data.recipe.imageUrl) {
                                navigator.navigate(RecipeDetailScreenDestination(recipeId = data.recipe.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(onSettingsIconClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = onSettingsIconClick) {
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
