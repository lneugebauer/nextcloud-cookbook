package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.CommonListItem
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue

@Composable
fun RecipesTopBar(categoryName: String?) {
    val title =
        if (categoryName == null) {
            stringResource(id = R.string.common_recipes)
        } else {
            "${stringResource(id = R.string.common_recipes)}: $categoryName"
        }

    TopAppBar(
        title = { Text(text = title) },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@ExperimentalMaterialApi
@Composable
fun RecipesScreen(
    navController: NavHostController,
    categoryName: String?,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = { RecipesTopBar(categoryName) }
    ) {
        if (state.data.isEmpty()) {
            Loader()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.padding_m),
                    vertical = dimensionResource(id = R.dimen.padding_s)
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