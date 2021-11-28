package de.lukasneugebauer.nextcloudcookbook.feature_settings.presentation

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.GITHUB_ISSUES_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.GITHUB_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.LICENSE_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.PRIVACY_URL

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
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back)
                )
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}

@Composable
fun SettingsContent(modifier: Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        SettingsGroupAccount()
        SettingsGroupAbout(context)
        SettingsGroupContribution(context)
    }
}

@Composable
fun SettingsGroupAccount() {
    SettingsGroup(title = { Text(text = stringResource(R.string.settings_account)) }) {
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
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

@Composable
fun SettingsGroupAbout(context: Context) {
    SettingsGroup(title = { Text(text = stringResource(id = R.string.common_about)) }) {
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.settings_privacy)
                )
            },
            title = { Text(text = stringResource(R.string.settings_privacy)) },
            onClick = { Uri.parse(PRIVACY_URL).openInBrowser(context) }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_license)) },
            subtitle = { Text(text = stringResource(R.string.settings_mit_license)) },
            onClick = { Uri.parse(LICENSE_URL).openInBrowser(context) }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_version)) },
            subtitle = {
                Text(
                    text = stringResource(
                        R.string.settings_version_number,
                        BuildConfig.VERSION_NAME
                    )
                )
            },
            onClick = {}
        )
    }
}

@Composable
fun SettingsGroupContribution(context: Context) {
    SettingsGroup(title = { Text(text = stringResource(R.string.settings_contribution)) }) {
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = stringResource(R.string.settings_source_code)
                )
            },
            title = { Text(text = stringResource(R.string.settings_source_code)) },
            subtitle = { Text(text = stringResource(R.string.settings_hosted_on_github)) },
            onClick = { Uri.parse(GITHUB_URL).openInBrowser(context) }
        )
        SettingsMenuLink(
            title = { Text(text = stringResource(R.string.settings_issues)) },
            subtitle = { Text(text = stringResource(R.string.settings_where_to_report_issues)) },
            onClick = { Uri.parse(GITHUB_ISSUES_URL).openInBrowser(context) }
        )
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    SettingsContent(modifier = Modifier)
}