package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.NotFoundScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeCreateScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeDetailScreenDestination
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import kotlin.random.Random.Default.nextInt

@Destination
@Composable
fun RecipeListScreen(
    navigator: DestinationsNavigator,
    categoryName: String?,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            RecipeListTopBar(
                categoryName = categoryName,
                onBackClick = {
                    navigator.popBackStack()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navigator.navigate(RecipeCreateScreenDestination)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) {
        RecipeListScreen(
            data = state.data,
            isLoading = state.loading,
            onClick = { id ->
                navigator.navigate(RecipeDetailScreenDestination(recipeId = id))
            }
        )
    }
}

@Composable
private fun RecipeListScreen(
    data: List<RecipePreview>,
    isLoading: Boolean,
    onClick: (Int) -> Unit
) {
    if (isLoading) {
        Loader()
    } else if (!isLoading && data.isEmpty()) {
        NotFoundScreen()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(data) { index, recipePreview ->
                ListItem(
                    modifier = Modifier.clickable(
                        onClick = {
                            onClick.invoke(recipePreview.id)
                        }
                    ),
                    icon = {
                        AuthorizedImage(
                            imageUrl = recipePreview.imageUrl,
                            contentDescription = recipePreview.name,
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.common_item_width_s))
                                .clip(MaterialTheme.shapes.medium)
                        )
                    },
                    secondaryText = {
                        Text(text = recipePreview.keywords.joinToString(separator = ", "))
                    },
                    singleLineSecondaryText = false,
                    text = {
                        Text(text = recipePreview.name)
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

@Composable
fun RecipeListTopBar(categoryName: String?, onBackClick: () -> Unit) {
    val title = categoryName ?: stringResource(id = R.string.common_recipes)

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = if (categoryName == null) {
            null
        } else {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_back)
                    )
                }
            }
        },
//        actions = {
//            IconButton(onClick = {}) {
//                Icon(
//                    Icons.Default.FilterList,
//                    contentDescription = stringResource(id = R.string.common_share)
//                )
//            }
//        },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}

@Preview
@Composable
fun RecipeListPreview() {
    val data = List(10) { id ->
        RecipePreview(
            id = id,
            name = "Recipe $id",
            keywords = List(nextInt(0, 5)) { "Keyword $it" },
            imageUrl = "",
            createdAt = "",
            modifiedAt = ""
        )
    }
    NextcloudCookbookTheme {
        RecipeListScreen(
            data = data,
            isLoading = false,
            onClick = {}
        )
    }
}
