package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Composable
fun NotFoundScreen() {
    AbstractErrorScreen(uiText = UiText.StringResource(R.string.error_no_recipes_found))
}

@Preview
@Composable
fun NotFoundScreenPreview() {
    NextcloudCookbookTheme {
        NotFoundScreen()
    }
}
