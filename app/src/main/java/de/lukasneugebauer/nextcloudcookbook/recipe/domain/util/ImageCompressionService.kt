package de.lukasneugebauer.nextcloudcookbook.recipe.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeImageUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.random.Random

private const val IMAGE_UPLOAD_MIME_TYPE = "image/jpeg"
private const val IMAGE_UPLOAD_QUALITY = 85
private const val IMAGE_UPLOAD_MAX_SIZE = 1600

class ImageCompressionService
    @Inject
    constructor(
        @ApplicationContext
        private val context: Context,
    ) {
        suspend fun compressRecipeImage(uri: Uri): RecipeImageUpload? =
            withContext(Dispatchers.IO) {
                val bounds =
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, bounds)
                    Unit
                } ?: return@withContext null

                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

                val decodeOptions =
                    BitmapFactory.Options().apply {
                        inSampleSize = bounds.calculateInSampleSize()
                    }

                val bitmap =
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream, null, decodeOptions)
                    } ?: return@withContext null

                val scaledBitmap = bitmap.scaleToMaxSize()
                val bytes =
                    ByteArrayOutputStream().use { outputStream ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_UPLOAD_QUALITY, outputStream)
                        outputStream.toByteArray()
                    }

                if (scaledBitmap !== bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle()

                RecipeImageUpload(
                    fileName = "recipe-image-${System.currentTimeMillis()}-${Random.nextInt(10000)}.jpg",
                    mimeType = IMAGE_UPLOAD_MIME_TYPE,
                    bytes = bytes,
                )
            }
    }

private fun BitmapFactory.Options.calculateInSampleSize(): Int {
    var inSampleSize = 1
    val largestSide = maxOf(outWidth, outHeight)

    while (largestSide / inSampleSize > IMAGE_UPLOAD_MAX_SIZE * 2) {
        inSampleSize *= 2
    }

    return inSampleSize
}

private fun Bitmap.scaleToMaxSize(): Bitmap {
    val largestSide = maxOf(width, height)
    if (largestSide <= IMAGE_UPLOAD_MAX_SIZE) return this

    val scale = IMAGE_UPLOAD_MAX_SIZE.toFloat() / largestSide
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}
