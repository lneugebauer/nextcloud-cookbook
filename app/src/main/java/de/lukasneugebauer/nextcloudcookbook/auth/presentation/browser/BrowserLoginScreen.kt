package de.lukasneugebauer.nextcloudcookbook.auth.presentation.browser

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StartScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.BrowserLoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.openInCustomTab

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.BrowserLoginScreen(
    navigator: DestinationsNavigator,
    url: String,
    viewModel: BrowserLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    HideBottomNavigation()

    // Collected from a single long-lived coroutine rather than a state-keyed LaunchedEffect: while
    // the Custom Tab is in front the activity is stopped and its recomposer's frame clock is
    // paused, so a state-keyed effect would not re-fire until the user came back. A coroutine that
    // is already suspended here resumes regardless, because AndroidUiDispatcher also posts to the
    // main-thread Handler.
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { state ->
            when (state) {
                is BrowserLoginScreenState.Loaded ->
                    if (!state.browserLaunched) {
                        if (state.loginUrl.openInCustomTab(context)) {
                            viewModel.onBrowserLaunched()
                        } else {
                            viewModel.onNoBrowserAvailable()
                        }
                    }

                BrowserLoginScreenState.Authenticated ->
                    navigator.navigate(HomeScreenDestination) {
                        popUpTo(StartScreenDestination) {
                            inclusive = true
                        }
                    }

                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(onBackClick = { navigator.navigateUp() })
        },
    ) { innerPadding ->
        when (uiState) {
            BrowserLoginScreenState.Initial, BrowserLoginScreenState.Authenticated -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
            is BrowserLoginScreenState.Loaded -> {
                BrowserLoginLayout(
                    modifier = Modifier.padding(innerPadding),
                    onOpenBrowserClick = viewModel::onOpenBrowserClick,
                )
            }
            is BrowserLoginScreenState.Error -> {
                val message = (uiState as BrowserLoginScreenState.Error).uiText
                AbstractErrorScreen(
                    uiText = message,
                    modifier = Modifier.padding(innerPadding),
                    onRetryClick = { viewModel.retry() },
                )
            }
        }
    }
}

@Composable
fun BrowserLoginLayout(
    modifier: Modifier = Modifier,
    onOpenBrowserClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.login_browser_waiting),
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        )
        Loader(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_l)))
        DefaultTextButton(onClick = onOpenBrowserClick) {
            Text(text = stringResource(R.string.login_browser_open))
        }
    }
}

@Composable
private fun TopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.login))
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                )
            }
        },
    )
}

@Preview
@Composable
private fun BrowserLoginLayoutPreview() {
    NextcloudCookbookTheme {
        BrowserLoginLayout(onOpenBrowserClick = {})
    }
}
