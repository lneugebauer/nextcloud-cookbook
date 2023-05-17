package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

@Composable
fun AbstractErrorScreen(
    uiText: UiText,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Warning,
    iconContentDescription: UiText? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription?.asString(),
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.error_icon_size))
                .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
            tint = MaterialTheme.colors.onBackground,
        )
        Text(text = uiText.asString())
    }
}

@Composable
@Preview
private fun UnknownErrorScreenPreview() {
    NextcloudCookbookTheme {
        AbstractErrorScreen(UiText.StringResource(R.string.error_unknown))
    }
}
