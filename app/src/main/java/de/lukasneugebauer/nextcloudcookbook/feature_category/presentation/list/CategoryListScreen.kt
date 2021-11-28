package de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.CommonListItem
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700

@ExperimentalMaterialApi
@Composable
fun CategoryListScreen(
    navController: NavHostController,
    viewModel: CategoryListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = { CategoryListTopBar() }
    ) {
        if (state.data.isEmpty()) {
            Loader()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(id = R.dimen.padding_m),
                    vertical = dimensionResource(id = R.dimen.padding_m)
                ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s))
            ) {
                items(state.data) {
                    CommonListItem(
                        name = it.name,
                        imageUrl = null
                    ) {
                        navController.navigate("${NextcloudCookbookScreen.Recipes.name}?categoryName=${it.name}")
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