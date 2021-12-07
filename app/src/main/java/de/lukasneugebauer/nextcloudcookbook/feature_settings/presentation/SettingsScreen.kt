package de.lukasneugebauer.nextcloudcookbook.feature_settings.presentation

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.SHARED_PREFERENCES_KEY
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.GITHUB_ISSUES_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.GITHUB_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.LICENSE_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.PRIVACY_URL
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.STAY_AWAKE_DEFAULT
import de.lukasneugebauer.nextcloudcookbook.feature_settings.util.SettingsConstants.STAY_AWAKE_KEY

@Composable
fun SettingsScreen(
    navController: NavHostController,
    sharedPreferences: SharedPreferences,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { SettingsTopBar(onNavIconClick = { navController.popBackStack() }) }
    ) { innerPadding ->
        SettingsContent(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            onLogoutClick = {
                viewModel.logout()
                navController.navigate(NextcloudCookbookScreen.Login.name) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            },
            sharedPreferences = sharedPreferences,
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
fun SettingsContent(
    modifier: Modifier,
    onLogoutClick: () -> Unit,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        SettingsGroupGeneral(sharedPreferences)
        SettingsGroupAccount(onLogoutClick)
        SettingsGroupAbout(context)
        SettingsGroupContribution(context)
    }
}

@Composable
fun SettingsGroupGeneral(sharedPreferences: SharedPreferences) {
    val stayAwakeState = rememberPreferenceBooleanSettingState(
        key = STAY_AWAKE_KEY,
        defaultValue = STAY_AWAKE_DEFAULT,
        preferences = sharedPreferences
    )

    SettingsGroup(title = { Text(text = stringResource(R.string.settings_general)) }) {
        SettingsSwitch(
            state = stayAwakeState,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LightMode,
                    contentDescription = stringResource(R.string.settings_stay_awake)
                )
            },
            title = { Text(text = stringResource(R.string.settings_stay_awake)) },
            subtitle = { Text(text = stringResource(R.string.settings_stay_awake_on_recipe_screen)) },
            onCheckedChange = {}
        )
    }
}

@Composable
fun SettingsGroupAccount(onLogoutClick: () -> Unit) {
    SettingsGroup(title = { Text(text = stringResource(R.string.settings_account)) }) {
        SettingsMenuLink(
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = stringResource(id = R.string.settings_logout)
                )
            },
            title = { Text(text = stringResource(id = R.string.settings_logout)) },
            onClick = onLogoutClick
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
            subtitle = {
                Text(
                    text = stringResource(R.string.settings_where_to_report_issues),
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_m))
                )
            },
            onClick = { Uri.parse(GITHUB_ISSUES_URL).openInBrowser(context) }
        )
    }
    Gap(size = dimensionResource(id = R.dimen.padding_s))
}

@Preview
@Composable
fun SettingsContentPreview() {
    val sharedPreferences =
        LocalContext.current.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    SettingsContent(modifier = Modifier, onLogoutClick = {}, sharedPreferences = sharedPreferences)
}