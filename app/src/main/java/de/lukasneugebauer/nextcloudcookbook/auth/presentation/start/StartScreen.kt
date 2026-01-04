package de.lukasneugebauer.nextcloudcookbook.auth.presentation.start

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.InfoScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ManualLoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WebViewLoginScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenSignInEvent
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.StartScreen(
    navigator: DestinationsNavigator,
    viewModel: StartScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(uiState) {
        if (uiState is StartScreenState.Loaded) {
            val data = uiState as StartScreenState.Loaded

            when (data.event) {
                StartScreenSignInEvent.WebView -> {
                    viewModel.onNavigate()
                    navigator.navigate(
                        WebViewLoginScreenDestination(
                            url = data.url,
                            allowSelfSignedCertificates = data.allowSelfSignedCertificates,
                        ),
                    )
                }
                StartScreenSignInEvent.Manual -> {
                    viewModel.onNavigate()
                    navigator.navigate(
                        ManualLoginScreenDestination(
                            url = data.url,
                            allowSelfSignedCertificates = data.allowSelfSignedCertificates,
                        ),
                    )
                }
                null -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(onClick = { navigator.navigate(InfoScreenDestination) })
        },
    ) { innerPadding ->
        when (uiState) {
            is StartScreenState.Loaded -> {
                val data = uiState as StartScreenState.Loaded
                StartLayout(
                    url = data.url,
                    allowSelfSignedCertificates = data.allowSelfSignedCertificates,
                    modifier = Modifier.padding(innerPadding),
                    urlError = data.urlError,
                    onUrlChange = { newUrl -> viewModel.onUrlChange(newUrl) },
                    onAllowSelfSignedCertificatesChange = { allowSelfSignedCertificates ->
                        viewModel.onAllowSelfSignedCertificatesChange(allowSelfSignedCertificates)
                    },
                    onWebViewLoginClick = { viewModel.onLoginClick(event = StartScreenSignInEvent.WebView) },
                    onManualLoginClick = { viewModel.onLoginClick(event = StartScreenSignInEvent.Manual) },
                )
            }
            is StartScreenState.Error -> {
                val message = (uiState as StartScreenState.Error).uiText
                AbstractErrorScreen(uiText = message, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun StartLayout(
    url: String,
    allowSelfSignedCertificates: Boolean,
    modifier: Modifier = Modifier,
    urlError: UiText? = null,
    onUrlChange: (String) -> Unit,
    onAllowSelfSignedCertificatesChange: (Boolean) -> Unit,
    onWebViewLoginClick: () -> Unit,
    onManualLoginClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nextcloud_logo_symbol),
            contentDescription = "Nextcloud Logo",
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_s)),
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
        Text(
            text = "Nextcloud Cookbook",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        )
        DefaultOutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
            label = {
                Text(
                    text = stringResource(R.string.login_root_address),
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.login_example_url),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            },
            errorText = urlError?.asString(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { onWebViewLoginClick.invoke() },
                ),
            singleLine = true,
        )
        Row(
            modifier =
                Modifier
                    .padding(bottom = dimensionResource(R.dimen.padding_m))
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = allowSelfSignedCertificates,
                onCheckedChange = onAllowSelfSignedCertificatesChange,
            )
            Text(text = stringResource(R.string.login_allow_self_signed_certificates))
        }
        DefaultButton(
            onClick = onWebViewLoginClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        ) {
            Text(text = stringResource(R.string.login))
        }
        DefaultTextButton(onClick = onManualLoginClick) {
            Text(text = stringResource(R.string.login_manual))
        }
    }
}

@Composable
private fun TopAppBar(onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "",
                )
            }
        }
    }
}

@Preview
@Composable
private fun TopAppBarPreview() {
    NextcloudCookbookTheme {
        TopAppBar(onClick = {})
    }
}

@Preview
@Composable
private fun StartLayoutPreview() {
    NextcloudCookbookTheme {
        StartLayout(
            url = "",
            allowSelfSignedCertificates = false,
            onUrlChange = {},
            onAllowSelfSignedCertificatesChange = {},
            onWebViewLoginClick = {},
            onManualLoginClick = {},
        )
    }
}
