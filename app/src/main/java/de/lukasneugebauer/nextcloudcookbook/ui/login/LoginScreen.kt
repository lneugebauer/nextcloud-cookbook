package de.lukasneugebauer.nextcloudcookbook.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NcBlueGradient

@Composable
fun LoginScreen() {
    var manualLogin: Boolean by rememberSaveable { mutableStateOf(false) }
    var username: String by rememberSaveable { mutableStateOf("") }
    var password: String by rememberSaveable { mutableStateOf("") }
    var url: String by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .background(NcBlueGradient)
            .fillMaxSize(),
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
        // TODO: 05.10.21 Create composable for button
        // TODO: 05.10.21 Finalize styling of button
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
        ) {
            Text(text = "Login using Nextcloud Files App")
        }
        Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
        // TODO: 05.10.21 Create composable for text button
        // TODO: 05.10.21 Finalize styling of text button
        TextButton(
            onClick = { manualLogin = !manualLogin }
        ) {
            Text(text = "Manual login")
        }
        if (manualLogin) {
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
            // TODO: 05.10.21 Create composable for text field
            // TODO: 05.10.21 Finalize styling of text field
            TextField(
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
                }
            )
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
            // TODO: 05.10.21 Create composable for text field
            // TODO: 05.10.21 Finalize styling of text field
            TextField(
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
                }
            )
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
            // TODO: 05.10.21 Create composable for text field
            // TODO: 05.10.21 Finalize styling of text field
            TextField(
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
                }
            )
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
            // TODO: 05.10.21 Create composable for button
            // TODO: 05.10.21 Finalize styling of button
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            ) {
                Text(text = "Sign in")
            }
        }
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreen()
}