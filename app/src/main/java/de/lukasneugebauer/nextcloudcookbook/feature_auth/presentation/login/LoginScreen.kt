package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultTextButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlueGradient

@Composable
fun LoginScreen(
    navController: NavController,
    onSsoClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var openSsoWarningDialog by rememberSaveable { mutableStateOf(false) }
    var manualLogin: Boolean by rememberSaveable { mutableStateOf(false) }
    val state = viewModel.state.value

    LaunchedEffect(key1 = state) {
        if (state.authorized) {
            navController.navigate(NextcloudCookbookScreen.Home.name)
        }
    }

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
        DefaultButton(
            onClick = {
                // onSSOClick()
                openSsoWarningDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
        ) {
            Text(text = "Login using Nextcloud Files App")
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        DefaultTextButton(onClick = { manualLogin = !manualLogin }) {
            Text(text = "Manual login")
        }
        if (manualLogin) {
            ManualLogin(viewModel)
        }
        if (openSsoWarningDialog) {
            AlertDialog(
                onDismissRequest = {
                    openSsoWarningDialog = false
                },
                title = {
                    Text(text = "Warning")
                },
                text = {
                    Text(text = "Single sign on currently doesn't work with kotlin. Please use manual login.\n\nSee current status on GitHub: https://github.com/nextcloud/Android-SingleSignOn/issues/177")
                },
                confirmButton = { },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openSsoWarningDialog = false
                        }
                    ) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ManualLogin(viewModel: LoginViewModel) {
    val focusManager = LocalFocusManager.current
    var username: String by rememberSaveable { mutableStateOf("") }
    var password: String by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var url: String by rememberSaveable { mutableStateOf("") }
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
    DefaultOutlinedTextField(
        value = username,
        onValueChange = { username = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Username",
                color = Color.White
            )
        },
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
        onValueChange = { password = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Password",
                color = Color.White
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
        onValueChange = { url = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = {
            Text(
                text = "Nextcloud root address",
                color = Color.White
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.manualLogin(username, password, url)
            }
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
    DefaultButton(
        onClick = { viewModel.manualLogin(username, password, url) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
    ) {
        Text(text = "Sign in")
    }
}

// @Composable
// @Preview
// fun LoginScreenPreview() {
//     LoginScreen(onSSOClick = {})
// }