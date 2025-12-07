package de.lukasneugebauer.nextcloudcookbook.auth.presentation.manual

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StartScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.ManualLoginScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.error.AbstractErrorScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.ManualLoginScreen(
    navigator: DestinationsNavigator,
    url: String,
    allowSelfSignedCertificates: Boolean,
    viewModel: ManualLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(uiState) {
        if (uiState is ManualLoginScreenState.Authenticated) {
            navigator.navigate(HomeScreenDestination) {
                popUpTo(StartScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(onBackClick = { navigator.navigateUp() }) },
    ) { innerPadding ->
        when (uiState) {
            is ManualLoginScreenState.Loaded -> {
                val (username, password, usernameError, passwordError) = uiState as ManualLoginScreenState.Loaded
                ManualLoginLayout(
                    url = url,
                    username = username,
                    password = password,
                    modifier = Modifier.padding(innerPadding),
                    usernameError = usernameError,
                    passwordError = passwordError,
                    onUsernameChange = { newUsername ->
                        viewModel.onUsernameChange(newUsername)
                    },
                    onPasswordChange = { newPassword ->
                        viewModel.onPasswordChange(newPassword)
                    },
                    onLoginClick = {
                        viewModel.tryManualLogin()
                    },
                )
            }
            ManualLoginScreenState.Authenticated, is ManualLoginScreenState.Authenticating -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
            is ManualLoginScreenState.Error -> {
                val message = (uiState as ManualLoginScreenState.Error).uiText
                AbstractErrorScreen(
                    uiText = message,
                    modifier = Modifier.padding(innerPadding),
                    onRetryClick = { viewModel.onRetry() },
                )
            }
        }
    }
}

@Composable
fun ManualLoginLayout(
    url: String,
    username: String,
    password: String,
    modifier: Modifier = Modifier,
    usernameError: UiText? = null,
    passwordError: UiText? = null,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_m)),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.login_instance_hint, url),
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        )
        DefaultOutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_s)).fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.common_username))
            },
            errorText = usernameError?.asString(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                ),
            singleLine = true,
        )
        DefaultOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)).fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.common_password))
            },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisibility) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }

                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(imageVector = image, "Show/hide password")
                }
            },
            errorText = passwordError?.asString(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    },
                ),
            singleLine = true,
        )
        DefaultButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.login))
        }
    }
}

@Composable
fun TopAppBar(onBackClick: () -> Unit) {
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

@Preview(showBackground = true)
@Composable
private fun ManualLoginLayoutPreview() {
    NextcloudCookbookTheme {
        ManualLoginLayout(
            url = "https://cloud.example.com",
            username = "foo",
            password = "bar",
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
        )
    }
}
