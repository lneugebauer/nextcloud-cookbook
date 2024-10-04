package de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*@SuppressLint("ConflictingOnColor")
private val DarkColorPalette =
    darkColors(
        primary = NcBlue700,
        primaryVariant = NcBlue800,
        secondary = NcBlue700,
        secondaryVariant = NcBlue800,
        background = Color.Black,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
    )

private val LightColorPalette =
    lightColors(
        primary = NcBlue700,
        primaryVariant = NcBlue800,
        secondary = NcBlue700,
        secondaryVariant = NcBlue800,
        background = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
    )
*/
@SuppressLint("NewApi")
@Composable
fun NextcloudCookbookTheme(
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
            // Use dynamic color scheme for devices running Android 12 (API 31) and above
            if (isSystemInDarkTheme()) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }



    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme as ColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}

