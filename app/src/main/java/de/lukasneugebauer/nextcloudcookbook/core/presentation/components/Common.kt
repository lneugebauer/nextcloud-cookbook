package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import de.lukasneugebauer.nextcloudcookbook.R
import timber.log.Timber

@Composable
fun CommonItem(
    name: String,
    imageUrl: String,
    width: Dp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(width)
    ) {
        Column {
            AuthorizedImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(width)
            )
            CommonItemBody(
                name = name,
                modifier = Modifier.width(width)
            ) {
                Timber.d("More icon clicked")
            }
        }
    }
}

@Composable
fun CommonItemBody(name: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = name,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_s))
                .weight(2f)
                .wrapContentWidth(Alignment.Start),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.body1
        )
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End)
        ) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = stringResource(id = R.string.common_more)
            )
        }
    }
}
