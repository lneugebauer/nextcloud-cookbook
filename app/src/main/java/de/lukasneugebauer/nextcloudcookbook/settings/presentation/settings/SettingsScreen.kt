package de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.LibrariesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SplashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.settings.domain.state.SettingsScreenState
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_ISSUES_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_SPONSOR_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.LIBERAPAY_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.LICENSE_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.PAYPAL_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.PRIVACY_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.WEBLATE_URL

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.SettingsScreen(
    navigator: DestinationsNavigator,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    Scaffold(
        topBar = { SettingsTopBar(onNavIconClick = { navigator.navigateUp() }) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        when (uiState) {
            is SettingsScreenState.Initial -> {
                Loader(modifier = Modifier.padding(innerPadding))
            }
            is SettingsScreenState.Loaded -> {
                val currentState = uiState as SettingsScreenState.Loaded
                SettingsLayout(
                    modifier = Modifier.padding(innerPadding),
                    isStayAwake = currentState.isStayAwake,
                    onStayAwakeChange = { isStayAwake ->
                        viewModel.setStayAwake(isStayAwake)
                    },
                    isShowRecipeSyntaxIndicator = currentState.isShowRecipeSyntaxIndicator,
                    onShowRecipeSyntaxIndicatorChange = { isShowRecipeSyntaxIndicator ->
                        viewModel.setShowRecipeSyntaxIndicator(isShowRecipeSyntaxIndicator)
                    },
                    onLogoutClick = {
                        viewModel.logout {
                            navigator.navigate(LoginScreenDestination) {
                                popUpTo(SplashScreenDestination) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onPrivacyClick = {
                        PRIVACY_URL.toUri().openInBrowser(context)
                    },
                    onLicenseClick = {
                        LICENSE_URL.toUri().openInBrowser(context)
                    },
                    onLibrariesClick = {
                        navigator.navigate(LibrariesScreenDestination)
                    },
                    onSourceCodeClick = {
                        GITHUB_URL.toUri().openInBrowser(context)
                    },
                    onTranslateClick = {
                        WEBLATE_URL.toUri().openInBrowser(context)
                    },
                    onIssuesClick = {
                        GITHUB_ISSUES_URL.toUri().openInBrowser(context)
                    },
                    onGitHubClick = {
                        GITHUB_SPONSOR_URL.toUri().openInBrowser(context)
                    },
                    onLiberapayClick = {
                        LIBERAPAY_URL.toUri().openInBrowser(context)
                    },
                    onPayPalClick = {
                        PAYPAL_URL.toUri().openInBrowser(context)
                    },
                )
            }
        }
    }
}

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
    )
}

@Composable
fun SettingsLayout(
    modifier: Modifier = Modifier,
    isStayAwake: Boolean,
    onStayAwakeChange: (Boolean) -> Unit,
    isShowRecipeSyntaxIndicator: Boolean,
    onShowRecipeSyntaxIndicatorChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    onSourceCodeClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onIssuesClick: () -> Unit,
    onGitHubClick: () -> Unit,
    onLiberapayClick: () -> Unit,
    onPayPalClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .then(modifier),
    ) {
        SettingsGroupGeneral(
            isStayAwake = isStayAwake,
            onStayAwakeChange = onStayAwakeChange,
            isShowRecipeSyntaxIndicator = isShowRecipeSyntaxIndicator,
            onShowRecipeSyntaxIndicatorChange = onShowRecipeSyntaxIndicatorChange,
        )
        Spacer(modifier = Modifier.size(size = dimensionResource(R.dimen.padding_m)))
        SettingsGroupAccount(onLogoutClick = onLogoutClick)
        Spacer(modifier = Modifier.size(size = dimensionResource(R.dimen.padding_m)))
        SettingsGroupAbout(
            onPrivacyClick = onPrivacyClick,
            onLicenseClick = onLicenseClick,
            onLibrariesClick = onLibrariesClick,
        )
        Spacer(modifier = Modifier.size(size = dimensionResource(R.dimen.padding_m)))
        SettingsGroupContribution(
            onSourceCodeClick = onSourceCodeClick,
            onTranslateClick = onTranslateClick,
            onIssuesClick = onIssuesClick,
        )
        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (BuildConfig.FLAVOR == "full") {
            Spacer(modifier = Modifier.size(size = dimensionResource(R.dimen.padding_m)))
            SettingsGroupSponsoring(
                onGitHubClick = onGitHubClick,
                onLiberapayClick = onLiberapayClick,
                onPayPalClick = onPayPalClick,
            )
        }
    }
}

@Composable
fun ColumnScope.SettingsGroupGeneral(
    isStayAwake: Boolean,
    onStayAwakeChange: (Boolean) -> Unit,
    isShowRecipeSyntaxIndicator: Boolean,
    onShowRecipeSyntaxIndicatorChange: (Boolean) -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_general),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_stay_awake))
        },
        supportingContent = {
            Text(text = stringResource(R.string.settings_stay_awake_on_recipe_screen))
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.LightMode,
                contentDescription = null,
            )
        },
        trailingContent = {
            Switch(
                checked = isStayAwake,
                onCheckedChange = onStayAwakeChange,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = "Recipe syntax indicator")
        },
        supportingContent = {
            Text(text = "Show an indicator if the recipe syntax is invalid")
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Report,
                contentDescription = null,
            )
        },
        trailingContent = {
            Switch(
                checked = isShowRecipeSyntaxIndicator,
                onCheckedChange = onShowRecipeSyntaxIndicatorChange,
            )
        },
    )
}

@Composable
fun ColumnScope.SettingsGroupAccount(onLogoutClick: () -> Unit) {
    Text(
        text = stringResource(R.string.settings_account),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_logout))
        },
        modifier = Modifier.clickable(onClick = onLogoutClick),
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
            )
        },
    )
}

@Composable
fun ColumnScope.SettingsGroupAbout(
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onLibrariesClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.common_about),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_privacy))
        },
        modifier = Modifier.clickable(onClick = onPrivacyClick),
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_license))
        },
        modifier = Modifier.clickable(onClick = onLicenseClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_mit_license))
        },
        leadingContent = {
            Spacer(modifier = Modifier.size(size = 24.dp))
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_oss_licenses))
        },
        modifier = Modifier.clickable(onClick = onLibrariesClick),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_version))
        },
        supportingContent = {
            Text(
                text =
                    stringResource(
                        R.string.settings_version_number,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                    ),
            )
        },
        leadingContent = {
            Spacer(modifier = Modifier.size(size = 24.dp))
        },
    )
}

@Composable
fun ColumnScope.SettingsGroupContribution(
    onSourceCodeClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onIssuesClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_contribution),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_source_code))
        },
        modifier = Modifier.clickable(onClick = onSourceCodeClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_hosted_on_github))
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_translate))
        },
        modifier = Modifier.clickable(onClick = onTranslateClick),
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Translate,
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_issues))
        },
        modifier = Modifier.clickable(onClick = onIssuesClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_where_to_report_issues))
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
            )
        },
    )
}

@Composable
fun ColumnScope.SettingsGroupSponsoring(
    onGitHubClick: () -> Unit,
    onLiberapayClick: () -> Unit,
    onPayPalClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.settings_sponsoring),
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_github))
        },
        modifier = Modifier.clickable(onClick = onGitHubClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_github_sponsor))
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_github_24),
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_liberapay))
        },
        modifier = Modifier.clickable(onClick = onLiberapayClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_liberapay_patron))
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_liberapay_24),
                contentDescription = null,
            )
        },
    )
    ListItem(
        headlineContent = {
            Text(text = stringResource(R.string.settings_paypal))
        },
        modifier = Modifier.clickable(onClick = onPayPalClick),
        supportingContent = {
            Text(text = stringResource(R.string.settings_paypal_donation))
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_paypal_24),
                contentDescription = null,
            )
        },
    )
}

@PreviewScreenSizes
@Composable
private fun SettingsContentPreview() {
    NextcloudCookbookTheme {
        SettingsLayout(
            isStayAwake = false,
            onStayAwakeChange = {},
            isShowRecipeSyntaxIndicator = true,
            onShowRecipeSyntaxIndicatorChange = {},
            onLogoutClick = {},
            onPrivacyClick = {},
            onLicenseClick = {},
            onLibrariesClick = {},
            onSourceCodeClick = {},
            onTranslateClick = {},
            onIssuesClick = {},
            onGitHubClick = {},
            onLiberapayClick = {},
            onPayPalClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsGroupSponsoringPreview() {
    NextcloudCookbookTheme {
        Column {
            SettingsGroupSponsoring(
                onGitHubClick = {},
                onLiberapayClick = {},
                onPayPalClick = {},
            )
        }
    }
}
