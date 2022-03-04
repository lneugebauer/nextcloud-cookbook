package de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants
import okhttp3.Credentials

@ExperimentalCoilApi
@Composable
fun authorizedImagePainter(
    baseUrl: String?,
    imageUrl: String,
    username: String?,
    token: String?
): Painter {
    val painter = rememberImagePainter(
        data = baseUrl + imageUrl,
        builder = {
            val credentials = Credentials.basic(username ?: "", token ?: "")
            addHeader("Authorization", credentials)
            crossfade(Constants.CROSSFADE_DURATION_MILLIS)
        }
    )

    val painterState = painter.state
    if (painterState is ImagePainter.State.Error) {
        return rememberDrawablePainter(
            drawable = AppCompatResources.getDrawable(
                LocalContext.current,
                R.drawable.common_image_placeholder
            )
        )
    }

    return painter
}

@ExperimentalCoilApi
@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
    viewModel: AuthorizedImageViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Image(
        painter = authorizedImagePainter(
            state.account?.url,
            imageUrl,
            state.account?.username,
            state.account?.token
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
