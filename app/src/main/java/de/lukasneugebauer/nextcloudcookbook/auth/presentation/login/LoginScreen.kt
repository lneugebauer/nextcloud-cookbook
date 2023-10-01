package de.lukasneugebauer.nextcloudcookbook.auth.presentation.login

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlueGradient
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.destinations.HomeScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = SwipeableDefaults.AnimationSpec,
        confirmValueChange = {
            when (it) {
                ModalBottomSheetValue.Hidden -> true
                ModalBottomSheetValue.Expanded -> true
                ModalBottomSheetValue.HalfExpanded -> false
            }
        },
        skipHalfExpanded = true,
    )
    var showManualLogin: Boolean by rememberSaveable { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(key1 = uiState) {
        // Navigate to home screen if user authorized
        if (uiState.authorized) {
            navigator.navigate(HomeScreenDestination()) {
                popUpTo(LoginScreenDestination.route) {
                    inclusive = true
                }
            }
        }

        // Open modal bottom sheet as soon as an url is available
        if (uiState.webViewUrl != null) {
            sheetState.show()
        }

        // Close modal bottom sheet if currently open and url is unavailable
        if (sheetState.currentValue == ModalBottomSheetValue.Expanded && uiState.webViewUrl == null) {
            sheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            LoginWebView(
                url = uiState.webViewUrl,
                onCloseClick = {
                    scope.launch { sheetState.hide() }
                    viewModel.onHideWebView()
                },
            )
        },
        sheetState = sheetState,
    ) {
        LoginScreen(
            showManualLogin = showManualLogin,
            usernameError = uiState.usernameError,
            passwordError = uiState.passwordError,
            urlError = uiState.urlError,
            onClearError = viewModel::clearErrors,
            onLoginClick = { url ->
                viewModel.getLoginEndpoint(url)
                keyboardController?.hide()
            },
            onShowManualLoginClick = { showManualLogin = !showManualLogin },
            onManualLoginClick = { username, password, url ->
                viewModel.tryManualLogin(username, password, url)
                keyboardController?.hide()
            },
        )
    }
}

@Composable
private fun LoginScreen(
    showManualLogin: Boolean,
    usernameError: UiText?,
    passwordError: UiText?,
    urlError: UiText?,
    onClearError: () -> Unit,
    onLoginClick: (url: String) -> Unit,
    onShowManualLoginClick: () -> Unit,
    onManualLoginClick: (username: String, password: String, url: String) -> Unit,
) {
    var url: String by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(NcBlueGradient)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nextcloud_logo_symbol),
            contentDescription = "Nextcloud Logo",
            alignment = Alignment.Center,
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        Text(
            text = "Nextcloud Cookbook",
            color = Color.White,
            style = MaterialTheme.typography.h5,
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_l)))
        DefaultOutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                onClearError.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
            label = {
                Text(
                    text = stringResource(R.string.login_root_address),
                    color = MaterialTheme.colors.onPrimary,
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.login_example_url),
                    color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f),
                )
            },
            errorText = urlError?.asString(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginClick.invoke(url) },
            ),
            singleLine = true,
        )
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        DefaultButton(
            onClick = { onLoginClick.invoke(url) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        ) {
            Text(text = stringResource(R.string.login))
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
        DefaultTextButton(onClick = onShowManualLoginClick) {
            Text(text = stringResource(R.string.login_manual))
        }
        if (showManualLogin) {
            ManualLoginForm(
                usernameError = usernameError,
                passwordError = passwordError,
                urlError = urlError,
                onClearError = onClearError,
                onManualLoginClick = onManualLoginClick,
            )
        }
    }
}

@Composable
private fun ManualLoginForm(
    usernameError: UiText?,
    passwordError: UiText?,
    urlError: UiText?,
    onClearError: () -> Unit,
    onManualLoginClick: (username: String, password: String, url: String) -> Unit,
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
            onClearError.invoke()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = stringResource(R.string.common_username),
                color = MaterialTheme.colors.onPrimary,
            )
        },
        errorText = usernameError?.asString(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            onClearError.invoke()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = stringResource(R.string.common_password),
                color = MaterialTheme.colors.onPrimary,
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
        errorText = passwordError?.asString(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = url,
        onValueChange = {
            url = it
            onClearError.invoke()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = stringResource(R.string.login_root_address),
                color = MaterialTheme.colors.onPrimary,
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.login_example_url),
                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.5f),
            )
        },
        errorText = urlError?.asString(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onManualLoginClick(username, password, url)
            },
        ),
        singleLine = true,
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultButton(
        onClick = { onManualLoginClick(username, password, url) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
    ) {
        Text(text = stringResource(R.string.login_manual))
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(url: Uri?, onCloseClick: () -> Unit) {
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
                backgroundColor = NcBlue700,
                contentColor = Color.White,
            )
        },
    ) { innerPadding ->
        if (url != null) {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        settings.javaScriptEnabled = true
                        loadUrl(url.toString())
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                return false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            Loader()
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    NextcloudCookbookTheme {
        LoginScreen(false, null, null, null, {}, {}, {}, { _, _, _ -> })
    }
}

@Preview
@Composable
private fun LoginScreenDarkModePreview() {
    NextcloudCookbookTheme(darkTheme = true) {
        LoginScreen(false, null, null, null, {}, {}, {}, { _, _, _ -> })
    }
}
