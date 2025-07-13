package de.lukasneugebauer.nextcloudcookbook.auth.presentation.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StartScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.WebViewClient
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.WebViewScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.WebViewLoginScreen(
    navigator: DestinationsNavigator,
    url: String,
    allowSelfSignedCertificates: Boolean,
    viewModel: WebViewLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(uiState) {
        if (uiState is WebViewScreenState.Authenticated) {
            navigator.navigate(HomeScreenDestination) {
                popUpTo(StartScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(onBackClick = { navigator.navigateUp() })
        },
    ) { innerPadding ->
        when (uiState) {
            WebViewScreenState.Initial, WebViewScreenState.Authenticated -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
            is WebViewScreenState.Loaded -> {
                val webViewUrl = (uiState as WebViewScreenState.Loaded).webViewUrl
                WebViewLoginLayout(
                    url = webViewUrl,
                    allowSelfSignedCertificates = allowSelfSignedCertificates,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is WebViewScreenState.Error -> {
                val message = (uiState as WebViewScreenState.Error).uiText
                AbstractErrorScreen(uiText = message, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewLoginLayout(
    url: Uri,
    allowSelfSignedCertificates: Boolean,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient(allowSelfSignedCertificates = allowSelfSignedCertificates)
                loadUrl(url.toString())
            }
        },
        modifier = Modifier.fillMaxSize().then(modifier),
    )
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
