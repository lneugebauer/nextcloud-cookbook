package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.saket.telephoto.zoomable.rememberZoomableImageState
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorizedImageRequest
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val context = LocalContext.current
        val credentials = LocalCredentials.current

        // most likely a cache hit anyway
        val imageRequest = authorizedImageRequest(imageUrl, context, credentials)

        ZoomableAsyncImage(
            modifier = Modifier.fillMaxSize(),
            state = rememberZoomableImageState(),
            model = imageRequest,
            contentDescription = contentDescription,
        )
    }
}
