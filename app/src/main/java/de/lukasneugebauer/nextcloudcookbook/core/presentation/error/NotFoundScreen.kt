package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Composable
fun NotFoundScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AbstractErrorScreen(
            uiText = UiText.StringResource(R.string.error_no_recipes_found),
            icon = Icons.AutoMirrored.Filled.HelpOutline,
        )
    }
}

@Preview
@Composable
fun NotFoundScreenPreview() {
    NextcloudCookbookTheme {
        NotFoundScreen()
    }
}
