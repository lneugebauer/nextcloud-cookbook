package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants

@Composable
fun authorizedImagePainter(imageUrl: String): Painter {
    val credentials = LocalCredentials.current

    val path = credentials?.baseUrl?.toUri()?.path
    val regex = """^$path""".toRegex()
    val newImageUrl = imageUrl.replace(regex, "")
    val fullImageUrl = credentials?.baseUrl + newImageUrl

    val painter = rememberImagePainter(
        data = fullImageUrl,
        builder = {
            credentials?.basic?.let {
                addHeader("Authorization", credentials.basic)
            }
            crossfade(Constants.CROSSFADE_DURATION_MILLIS)
        },
    )

    val painterState = painter.state
    if (painterState is ImagePainter.State.Error) {
        return rememberDrawablePainter(
            drawable = AppCompatResources.getDrawable(
                LocalContext.current,
                R.drawable.common_image_placeholder,
            ),
        )
    }

    return painter
}

@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
) {
    Image(
        painter = authorizedImagePainter(imageUrl),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}
