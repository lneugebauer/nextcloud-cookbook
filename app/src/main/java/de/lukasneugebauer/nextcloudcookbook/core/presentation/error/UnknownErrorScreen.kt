package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Composable
fun UnknownErrorScreen() {
    AbstractErrorScreen(uiText = UiText.StringResource(R.string.error_unknown))
}

@Composable
@Preview
private fun UnknownErrorScreenPreview() {
    NextcloudCookbookTheme {
        UnknownErrorScreen()
    }
}