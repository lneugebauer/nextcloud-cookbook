package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials

@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val credentials = LocalCredentials.current

    val path = credentials?.baseUrl?.toUri()?.path
    val regex = """^$path""".toRegex()
    val newImageUrl = imageUrl.replace(regex, "")
    val fullImageUrl = credentials?.baseUrl + newImageUrl

    SubcomposeAsyncImage(
        model =
            ImageRequest.Builder(context)
                .data(fullImageUrl)
                .addHeader("Authorization", credentials?.basic ?: "")
                .crossfade(true)
                .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Error -> {
                Image(
                    painter =
                        rememberDrawablePainter(
                            drawable =
                                AppCompatResources.getDrawable(
                                    context,
                                    R.drawable.common_image_placeholder,
                                ),
                        ),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = ContentScale.Crop,
                )
            }

            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}
