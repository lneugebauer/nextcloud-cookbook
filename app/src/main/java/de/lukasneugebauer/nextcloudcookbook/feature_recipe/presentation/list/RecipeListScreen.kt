package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.RecipePreview

@Composable
fun RecipeListScreen(
    navController: NavHostController,
    categoryName: String?,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    RecipeListScreen(
        categoryName = categoryName,
        data = state.data,
        isLoading = state.loading,
        onClick = { id ->
            navController.navigate("${NextcloudCookbookScreen.Recipe.name}?recipeId=${id}")
        }
    )
}

@OptIn(ExperimentalMaterialApi::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
private fun RecipeListScreen(
    categoryName: String?,
    data: List<RecipePreview>,
    isLoading: Boolean,
    onClick: (Int) -> Unit
) {
    Scaffold(
        topBar = { RecipeListTopBar(categoryName) }
    ) {
        if (isLoading) {
            Loader()
        } else if (!isLoading && data.isEmpty()) {
            NotFoundScreen()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(data) {
                    ListItem(
                        modifier = Modifier.clickable(
                            onClick = {
                                onClick.invoke(it.id)
                            }
                        ),
                        icon = {
                            AuthorizedImage(
                                imageUrl = it.imageUrl,
                                contentDescription = it.name,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.common_item_width_s))
                            )
                        },
                        secondaryText = {
                            Text(text = it.keywords.joinToString(separator = ", "))
                        },
                        singleLineSecondaryText = false,
                        text = {
                            Text(text = it.name)
                        }
                    )
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
