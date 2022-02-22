package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlueGradient
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.state.LoginWebViewState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsState()
    val webViewState by viewModel.loginWebViewState.collectAsState()

    var webViewVisible by remember { mutableStateOf(false) }
    var showManualLogin: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = uiState) {
        if (uiState.authorized) {
            navController.navigate(NextcloudCookbookScreen.Home.name)
        }
    }

    webViewVisible = when (webViewState) {
        is LoginWebViewState.Gone -> false
        is LoginWebViewState.Visible -> true
    }

    LoginScreen(
        showManualLogin = showManualLogin,
        usernameError = uiState.usernameError,
        passwordError = uiState.passwordError,
        urlError = uiState.urlError,
        onClearError = { viewModel.clearErrors() },
        onLoginClick = { url ->
            viewModel.getLoginEndpoint(url)
            focusManager.clearFocus()
        },
        onShowManualLoginClick = { showManualLogin = !showManualLogin },
        onManualLoginClick = { username, password, url ->
            viewModel.tryManualLogin(username, password, url)
        }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = webViewVisible,
            enter = slideInVertically(
                initialOffsetY = { with(density) { maxHeight.roundToPx() } }
            ),
            exit = slideOutVertically()
        ) {
            val wvs = webViewState as LoginWebViewState.Visible
            LoginWebViewScreen(wvs.url)
        }
    }
}

@Composable
private fun LoginScreen(
    showManualLogin: Boolean,
    usernameError: String?,
    passwordError: String?,
    urlError: String?,
    onClearError: () -> Unit,
    onLoginClick: (url: String) -> Unit,
    onShowManualLoginClick: () -> Unit,
    onManualLoginClick: (username: String, password: String, url: String) -> Unit
) {
    var url: String by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(NcBlueGradient)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nextcloud_logo_symbol),
            contentDescription = "Nextcloud Logo",
            alignment = Alignment.Center
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        Text(
            text = "Nextcloud Cookbook",
            color = Color.White,
            style = MaterialTheme.typography.h5
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_l)))
        DefaultOutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                onClearError()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
            label = {
                Text(
                    text = "Nextcloud root address",
                    color = MaterialTheme.colors.onPrimary
                )
            },
            placeholder = {
                Text(
                    text = "https://cloud.example.tld",
                    color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)
                )
            },
            errorText = urlError,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginClick(url) }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        DefaultButton(
            onClick = { onLoginClick(url) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
        ) {
            Text(text = "Sign in")
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
        DefaultTextButton(onClick = onShowManualLoginClick) {
            Text(text = "Manual login")
        }
        if (showManualLogin) {
            ManualLoginForm(
                usernameError = usernameError,
                passwordError = passwordError,
                urlError = urlError,
                onClearError = onClearError,
                onManualLoginClick = onManualLoginClick
            )
        }
    }
}

@Composable
private fun ManualLoginForm(
    usernameError: String?,
    passwordError: String?,
    urlError: String?,
    onClearError: () -> Unit,
    onManualLoginClick: (username: String, password: String, url: String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var username: String by rememberSaveable { mutableStateOf("") }
    var password: String by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var url: String by rememberSaveable { mutableStateOf("") }
    DefaultOutlinedTextField(
        value = username,
        onValueChange = {
            username = it
            onClearError()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Username",
                color = MaterialTheme.colors.onPrimary
            )
        },
        errorText = usernameError,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            onClearError()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Password",
                color = MaterialTheme.colors.onPrimary
            )
        },
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisibility) {
                Icons.Filled.Visibility
            } else {
                Icons.Filled.VisibilityOff
            }

            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                Icon(imageVector = image, "Show/hide password", tint = Color.White)
            }
        },
        errorText = passwordError,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = url,
        onValueChange = {
            url = it
            onClearError()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Nextcloud root address",
                color = MaterialTheme.colors.onPrimary
            )
        },
        placeholder = {
            Text(
                text = "https://cloud.example.tld",
                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)
            )
        },
        errorText = urlError,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onManualLoginClick(username, password, url)
            }
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultButton(
        onClick = { onManualLoginClick(username, password, url) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
    ) {
        Text(text = "Manual sign in")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebViewScreen(url: Uri) {
    AndroidView(
        factory = {
            WebView(it).apply {
                settings.javaScriptEnabled = true
                loadUrl(url.toString())
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(false, null, null, null, {}, {}, {}, { _, _, _ -> })
}