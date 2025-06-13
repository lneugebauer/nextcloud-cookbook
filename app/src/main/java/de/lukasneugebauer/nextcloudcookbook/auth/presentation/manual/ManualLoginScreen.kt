package de.lukasneugebauer.nextcloudcookbook.auth.presentation.manual

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
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
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Destination<MainGraph>
@Composable
fun ManualLoginScreen(
    navigator: DestinationsNavigator,
    url: String,
    allowSelfSignedCertificates: Boolean,
    viewModel: ManualLoginViewModel = hiltViewModel(),
) {
    ManualLoginLayout()
}

@Composable
fun ManualLoginLayout(modifier: Modifier = Modifier) {
    // TODO: Show form with username and password. Do authentication check after button click.
    //  Redirect to home screen if successful.
}

@Composable
private fun ManualLoginForm(
    usernameError: UiText?,
    passwordError: UiText?,
    url: String,
    onClearError: () -> Unit,
    onManualLoginClick: (username: String, password: String, url: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var username: String by rememberSaveable { mutableStateOf("") }
    var password: String by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    DefaultOutlinedTextField(
        value = username,
        onValueChange = {
            username = it
            onClearError.invoke()
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = stringResource(R.string.common_username),
            )
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
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            onClearError.invoke()
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = stringResource(R.string.common_password),
            )
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
                Icon(imageVector = image, "Show/hide password", tint = Color.White)
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
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultButton(
        onClick = { onManualLoginClick(username, password, url) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
    ) {
        Text(text = stringResource(R.string.login_manual))
    }
}

@Preview(showBackground = true)
@Composable
private fun ManualLoginFormPreview() {
    NextcloudCookbookTheme {
        Column {
            ManualLoginForm(
                usernameError = null,
                passwordError = null,
                url = "",
                onClearError = {},
                onManualLoginClick = { _, _, _ -> },
            )
        }
    }
}
