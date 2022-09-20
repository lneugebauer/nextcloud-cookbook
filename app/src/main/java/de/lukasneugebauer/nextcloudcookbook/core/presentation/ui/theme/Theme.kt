package de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = NcBlue700,
    primaryVariant = NcBlue800,
    secondary = NcBlue700,
    secondaryVariant = NcBlue800,
    background = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White
)

private val LightColorPalette = lightColors(
    primary = NcBlue700,
    primaryVariant = NcBlue800,
    secondary = NcBlue700,
    secondaryVariant = NcBlue800,
    background = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black
)

@Composable
fun NextcloudCookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
