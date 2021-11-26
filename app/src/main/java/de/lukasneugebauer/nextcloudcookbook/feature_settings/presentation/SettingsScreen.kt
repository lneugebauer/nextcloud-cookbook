package de.lukasneugebauer.nextcloudcookbook.feature_settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alorma.compose.settings.ui.SettingsMenuLink
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue

@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = { SettingsTopBar(onNavIconClick = { navController.popBackStack() }) }
    ) { innerPadding ->
        SettingsContent(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxWidth()
        )
    }
}

@Composable
fun SettingsTopBar(onNavIconClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.common_settings)) },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back)
                )
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@Composable
fun SettingsContent(modifier: Modifier) {
    Column(modifier = modifier) {
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = stringResource(id = R.string.settings_logout)
                )
            },
            title = { Text(text = stringResource(id = R.string.settings_logout)) },
            onClick = {
                // TODO: 27.11.21 Clear preferences manager and navigate to login screen
            }
        )
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    SettingsContent(modifier = Modifier)
}