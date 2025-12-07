package de.lukasneugebauer.nextcloudcookbook.core.presentation.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
    onRetryClick: (() -> Unit)? = null,
) {
    Scaffold(
        content = { innerPadding ->
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription?.asString(),
                    modifier =
                        Modifier
                            .size(dimensionResource(id = R.dimen.error_icon_size))
                            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = uiText.asString(),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (onRetryClick != null) {
                    Button(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_l)), onClick = onRetryClick) {
                        Text(text = stringResource(R.string.error_retry))
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun UnknownErrorScreenPreview() {
    NextcloudCookbookTheme {
        AbstractErrorScreen(uiText = UiText.StringResource(R.string.error_unknown), onRetryClick = {})
    }
}
