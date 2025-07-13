package de.lukasneugebauer.nextcloudcookbook.settings.presentation.info

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.LibrariesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings.SettingsGroupAbout
import de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings.SettingsGroupContribution
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_ISSUES_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.GITHUB_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.LICENSE_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.PRIVACY_URL
import de.lukasneugebauer.nextcloudcookbook.settings.util.SettingsConstants.WEBLATE_URL

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.InfoScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current

    HideBottomNavigation()

    Scaffold(
        topBar = { TopAppBar(onBackClick = { navigator.navigateUp() }) },
    ) { innerPadding ->
        InfoLayout(
            modifier = Modifier.padding(innerPadding),
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
        )
    }
}

@Composable
private fun InfoLayout(
    modifier: Modifier = Modifier,
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    onSourceCodeClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onIssuesClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .then(modifier),
    ) {
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
    }
}

@Composable
private fun TopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.info_headline))
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

@Preview
@Composable
private fun TopAppBarPreview() {
    NextcloudCookbookTheme {
        TopAppBar {}
    }
}

@Preview
@Composable
private fun InfoLayoutPreview() {
    NextcloudCookbookTheme {
        InfoLayout(
            onPrivacyClick = {},
            onLicenseClick = {},
            onLibrariesClick = {},
            onSourceCodeClick = {},
            onTranslateClick = {},
            onIssuesClick = {},
        )
    }
}
