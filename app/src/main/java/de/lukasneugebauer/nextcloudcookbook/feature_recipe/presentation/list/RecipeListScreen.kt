package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.CommonListItem
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700

@ExperimentalMaterialApi
@Composable
fun RecipeListScreen(
    navController: NavHostController,
    categoryName: String?,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { RecipeListTopBar(categoryName) }
    ) {
        if (state.loading) {
            Loader()
        }

        if (!state.loading && state.data.isEmpty()) {
            NotFoundScreen()
        }

        if (state.data.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.padding_m),
                    vertical = dimensionResource(id = R.dimen.padding_m)
                ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s))
            ) {
                items(state.data) {
                    CommonListItem(name = it.name, imageUrl = it.imageUrl) {
                        navController.navigate("${NextcloudCookbookScreen.Recipe.name}?recipeId=${it.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeListTopBar(categoryName: String?) {
    val title =
        if (categoryName == null) {
            stringResource(id = R.string.common_recipes)
        } else {
            "${stringResource(id = R.string.common_recipes)}: $categoryName"
        }

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = stringResource(id = R.string.common_share)
                )
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}