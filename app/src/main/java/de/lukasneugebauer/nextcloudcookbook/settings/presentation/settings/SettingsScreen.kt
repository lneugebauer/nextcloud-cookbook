package de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.SHARED_PREFERENCES_KEY
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.destinations.LibrariesScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.SplashScreenDestination
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_ISSUES_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.LICENSE_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.PRIVACY_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.STAY_AWAKE_DEFAULT
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.STAY_AWAKE_KEY
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.WEBLATE_URL

@Destination
@Composable
fun SettingsScreen(
    navigator: DestinationsNavigator,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = { SettingsTopBar(onNavIconClick = { navigator.navigateUp() }) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { innerPadding ->
        SettingsContent(
            modifier =
                Modifier
                    .padding(paddingValues = innerPadding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            onLibrariesClick = { navigator.navigate(LibrariesScreenDestination) },
            onLogoutClick = {
                viewModel.logout {
                    navigator.navigate(LoginScreenDestination) {
                        popUpTo(SplashScreenDestination) {
                            inclusive = true
                        }
                    }
                }
            },
            sharedPreferences = viewModel.sharedPreferences,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(onNavIconClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.common_settings)) },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
fun SettingsContent(
    modifier: Modifier,
    onLibrariesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    sharedPreferences: SharedPreferences,
) {
    val context = LocalContext.current

    Box(
        modifier =
            modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier) {
            SettingsGroupGeneral(sharedPreferences)
            SettingsGroupAccount(onLogoutClick)
            SettingsGroupAbout(context, onLibrariesClick)
            SettingsGroupContribution(context)
        }
    }
}

@Composable
fun SettingsGroupGeneral(sharedPreferences: SharedPreferences) {
    val stayAwakeState =
        rememberPreferenceBooleanSettingState(
            key = STAY_AWAKE_KEY,
            defaultValue = STAY_AWAKE_DEFAULT,
            preferences = sharedPreferences,
        )

    SettingsGroup(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = { Text(text = stringResource(R.string.settings_general)) },
    ) {
        SettingsSwitch(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            state = stayAwakeState,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LightMode,
                    contentDescription = stringResource(R.string.settings_stay_awake),
                )
            },
            title = { Text(text = stringResource(R.string.settings_stay_awake)) },
            subtitle = { Text(text = stringResource(R.string.settings_stay_awake_on_recipe_screen)) },
            onCheckedChange = {},
            switchColors =
                SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
        )
    }
}

@Composable
fun SettingsGroupAccount(onLogoutClick: () -> Unit) {
    SettingsGroup(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = { Text(text = stringResource(R.string.settings_account)) },
    ) {
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = stringResource(id = R.string.settings_logout),
                )
            },
            title = { Text(text = stringResource(id = R.string.settings_logout)) },
            onClick = onLogoutClick,
        )
    }
}

@Composable
fun SettingsGroupAbout(
    context: Context,
    onLibrariesClick: () -> Unit,
) {
    SettingsGroup(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = { Text(text = stringResource(id = R.string.common_about)) },
    ) {
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.settings_privacy),
                )
            },
            title = { Text(text = stringResource(R.string.settings_privacy)) },
            onClick = { Uri.parse(PRIVACY_URL).openInBrowser(context) },
        )
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {},
            title = { Text(text = stringResource(R.string.settings_license)) },
            subtitle = { Text(text = stringResource(R.string.settings_mit_license)) },
            onClick = { Uri.parse(LICENSE_URL).openInBrowser(context) },
        )
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = stringResource(id = R.string.settings_oss_licenses),
                )
            },
            title = { Text(text = stringResource(id = R.string.settings_oss_licenses)) },
            onClick = onLibrariesClick,
        )
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {},
            title = { Text(text = stringResource(R.string.settings_version)) },
            subtitle = {
                Text(
                    text =
                        stringResource(
                            R.string.settings_version_number,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE,
                        ),
                )
            },
            onClick = {},
        )
    }
}

@Composable
fun SettingsGroupContribution(context: Context) {
    SettingsGroup(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = { Text(text = stringResource(R.string.settings_contribution)) },
    ) {
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = stringResource(R.string.settings_source_code),
                )
            },
            title = { Text(text = stringResource(R.string.settings_source_code)) },
            subtitle = { Text(text = stringResource(R.string.settings_hosted_on_github)) },
            onClick = { Uri.parse(GITHUB_URL).openInBrowser(context) },
        )
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Translate,
                    contentDescription = stringResource(id = R.string.settings_translate),
                )
            },
            title = { Text(text = stringResource(id = R.string.settings_translate)) },
            onClick = { Uri.parse(WEBLATE_URL).openInBrowser(context) },
        )
        SettingsMenuLink(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = stringResource(R.string.settings_issues),
                )
            },
            title = { Text(text = stringResource(R.string.settings_issues)) },
            subtitle = {
                Text(
                    text = stringResource(R.string.settings_where_to_report_issues),
                    modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_m)),
                )
            },
            onClick = { Uri.parse(GITHUB_ISSUES_URL).openInBrowser(context) },
        )
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    val sharedPreferences =
        LocalContext.current.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    SettingsContent(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        onLibrariesClick = {},
        onLogoutClick = {},
        sharedPreferences = sharedPreferences,
    )
}
