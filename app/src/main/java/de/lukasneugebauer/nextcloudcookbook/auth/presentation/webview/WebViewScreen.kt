package de.lukasneugebauer.nextcloudcookbook.auth.presentation.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.WebViewScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.WebViewScreen(
    navigator: DestinationsNavigator,
    url: String,
    allowSelfSignedCertificates: Boolean,
    viewModel: WebViewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(uiState) {
        if (uiState is WebViewScreenState.Authorized) {
            navigator.navigate(HomeScreenDestination()) {
                popUpTo(LoginScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    // TODO: Add app bar with back button.
    // TODO: Redirect to home screen if login polling was successful.
    Scaffold { innerPadding ->
        when (uiState) {
            WebViewScreenState.Initial, WebViewScreenState.Authorized -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
            is WebViewScreenState.Loaded -> {
                WebViewLayout(modifier = Modifier.padding(innerPadding))
            }
            is WebViewScreenState.Error -> {
                val message = (uiState as WebViewScreenState.Error).uiText
                AbstractErrorScreen(uiText = message, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun WebViewLayout(modifier: Modifier = Modifier) {
    LoginWebView(url = null) { }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    url: Uri?,
    onCloseClick: () -> Unit,
) {
    BackHandler(onBack = onCloseClick)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.login))
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.common_close),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (url != null) {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        settings.javaScriptEnabled = true
                        loadUrl(url.toString())
                        webViewClient =
                            object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    return false
                                }
                            }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            )
        } else {
            Loader()
        }
    }
}
