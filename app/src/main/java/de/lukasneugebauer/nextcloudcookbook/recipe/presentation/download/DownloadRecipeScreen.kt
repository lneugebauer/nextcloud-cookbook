package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.download

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.ramcosta.composedestinations.generated.destinations.RecipeDetailScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeListScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.DownloadRecipeScreenState
import timber.log.Timber

@Destination<MainGraph>
@Composable
@Suppress("UNUSED_PARAMETER")
fun AnimatedVisibilityScope.DownloadRecipeScreen(
    navigator: DestinationsNavigator,
    sharedUrl: String? = null,
    viewModel: DownloadRecipeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // If a sharedUrl navigation argument is provided, populate the ViewModel's url field
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            // The incoming parameter can be URL-encoded and sometimes prefixed by
            // route placeholders like "{sharedUrl}?sharedUrl=" depending on how the
            // route was constructed. Normalize by extracting the last "sharedUrl="
            // value and URL-decoding it.
            Timber.d("Raw sharedUrl param: $sharedUrl")
            try {
                var candidate = sharedUrl
                // If the parameter accidentally contains the literal template or repeated params,
                // pick the substring after the last "sharedUrl=" occurrence.
                val marker = "sharedUrl="
                val idx = candidate.lastIndexOf(marker)
                if (idx != -1) {
                    candidate = candidate.substring(idx + marker.length)
                }

                // URL-decode (handle percent-encoding)
                candidate = java.net.URLDecoder.decode(candidate, "UTF-8")

                Timber.i("Normalized shared URL: $candidate")

                if (candidate.isNotBlank()) {
                    viewModel.updateUrl(candidate)
                    // Start the import automatically when the sharedUrl is provided
                    viewModel.importRecipe()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to normalize/decode sharedUrl: $sharedUrl")
                // Fallback: use the raw param if normalization fails
                viewModel.updateUrl(sharedUrl)
                viewModel.importRecipe()
            }
        }
    }

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
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is DownloadRecipeScreenState.Error -> {
                val errorState = (uiState as DownloadRecipeScreenState.Error)
                DownloadRecipeScreen(
                    url = errorState.url,
                    onDownloadClick = { viewModel.importRecipe() },
                    onUrlChange = { viewModel.updateUrl(it) },
                    modifier = Modifier.padding(innerPadding),
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
                    .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
            label = { Text(text = stringResource(R.string.download_recipe_url)) },
            errorText = error?.asString(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onDownloadClick.invoke() },
                ),
            singleLine = true,
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
