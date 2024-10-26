package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeDetailScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListScreenDestination
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState

@Destination
@Composable
fun DownloadRecipeScreen(
    navigator: DestinationsNavigator,
    viewModel: DownloadRecipeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    Scaffold(
        topBar = {
            RecipeDownloadTopBar {
                navigator.navigateUp()
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is DownloadRecipeScreenState.Initial -> {
                val url = (uiState as DownloadRecipeScreenState.Initial).url
                DownloadRecipeScreen(
                    url = url,
                    onDownloadClick = { viewModel.importRecipe() },
                    onUrlChange = { viewModel.updateUrl(it) },
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .padding(top = dimensionResource(id = R.dimen.padding_m)),
                )
            }
            is DownloadRecipeScreenState.Error -> {
                val errorState = (uiState as DownloadRecipeScreenState.Error)
                DownloadRecipeScreen(
                    url = errorState.url,
                    onDownloadClick = { viewModel.importRecipe() },
                    onUrlChange = { viewModel.updateUrl(it) },
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .padding(top = dimensionResource(id = R.dimen.padding_m)),
                    error = errorState.uiText,
                )
            }
            is DownloadRecipeScreenState.Loading -> {
                Loader()
            }
            is DownloadRecipeScreenState.Loaded -> {
                val id = (uiState as DownloadRecipeScreenState.Loaded).id
                LaunchedEffect(id) {
                    navigator.navigate(RecipeDetailScreenDestination(id)) {
                        popUpTo(RecipeListScreenDestination)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRecipeScreen(
    url: String,
    onDownloadClick: () -> Unit,
    onUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: UiText? = null,
) {
    Column(
        modifier = modifier,
    ) {
        DefaultOutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_xs)),
            label = {
                Text(
                    text = stringResource(R.string.download_recipe_url),
                )
            },
            errorText = error?.asString(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { },
                ),
            singleLine = true,
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Button(
            onClick = onDownloadClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
            enabled = url.isNotEmpty() && error == null,
        ) {
            Text(text = stringResource(R.string.common_download))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDownloadTopBar(onNavIconClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.download_recipe),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
    )
}

@Preview
@Composable
private fun DownloadRecipeScreenPreview() {
    NextcloudCookbookTheme {
        DownloadRecipeScreen(
            url = "https://example.com/recipe",
            onDownloadClick = {},
            onUrlChange = {},
        )
    }
}
