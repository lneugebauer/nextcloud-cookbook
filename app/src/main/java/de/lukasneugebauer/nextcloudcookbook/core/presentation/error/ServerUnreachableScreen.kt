package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Composable
fun ServerUnreachableScreen(onRetryClick: () -> Unit) {
    AbstractErrorScreen(
        uiText = UiText.StringResource(R.string.error_server_unreachable_description),
        icon = Icons.Default.CloudOff,
        iconContentDescription = UiText.StringResource(R.string.error_server_unreachable),
        onRetryClick = onRetryClick,
    )
}

@Composable
@Preview
private fun ServerUnreachableScreenPreview() {
    NextcloudCookbookTheme {
        ServerUnreachableScreen(onRetryClick = {})
    }
}
