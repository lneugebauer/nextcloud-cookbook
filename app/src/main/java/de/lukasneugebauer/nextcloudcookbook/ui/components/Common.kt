package de.lukasneugebauer.nextcloudcookbook.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import de.lukasneugebauer.nextcloudcookbook.utils.Logger

private const val TAG = "Common"

@Composable
fun CommonItem(name: String, imageUrl: Uri, width: Dp) {
    Card(
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
                Logger.d("More icon clicked", TAG)
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
            style = MaterialTheme.typography.body1,
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

@ExperimentalMaterialApi
@Composable
fun CommonListItem(name: String, modifier: Modifier = Modifier, imageUrl: Uri? = null, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (imageUrl != null) {
                AuthorizedImage(
                    imageUrl = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.common_item_width_s))
                )
            }
            Spacer(modifier = Modifier.size(size = dimensionResource(id = R.dimen.padding_s)))
            Text(
                text = name,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_s)),
                style = MaterialTheme.typography.h6
            )
        }
    }
}