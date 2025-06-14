package de.lukasneugebauer.nextcloudcookbook.auth.presentation.start

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.WebViewScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.StartScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
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
        when (uiState) {
            is StartScreenState.WebViewLogin -> {
                val data = (uiState as StartScreenState.WebViewLogin)
                viewModel.onNavigate()
                navigator.navigate(
                    WebViewScreenDestination(
                        url = data.url,
                        allowSelfSignedCertificates = data.allowSelfSignedCertificates
                    )
                )
            }
            is StartScreenState.ManualLogin -> {
                val data = (uiState as StartScreenState.ManualLogin)
                viewModel.onNavigate()
                navigator.navigate(
                    WebViewScreenDestination(
                        url = data.url,
                        allowSelfSignedCertificates = data.allowSelfSignedCertificates
                    )
                )
            }
            else -> Unit
        }
    }

    Scaffold { innerPadding ->
        when (uiState) {
            is StartScreenState.WebViewLogin, is StartScreenState.ManualLogin -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
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
                    onWebViewLoginClick = { viewModel.onWebViewLoginClick() },
                    onManualLoginClick = { viewModel.onManualLoginClick() },
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
            alignment = Alignment.Center,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        Text(
            text = "Nextcloud Cookbook",
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_l)))
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
            modifier = Modifier.fillMaxWidth(),
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
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
        DefaultTextButton(onClick = onManualLoginClick) {
            Text(text = stringResource(R.string.login_manual))
        }
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
