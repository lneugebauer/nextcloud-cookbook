package de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = NcBlue700,
    primaryVariant = NcBlue800,
    secondary = NcBlue700,
    secondaryVariant = NcBlue800
)

private val LightColorPalette = lightColors(
    primary = NcBlue700,
    primaryVariant = NcBlue800,
    secondary = NcBlue700,
    secondaryVariant = NcBlue800

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun NextcloudCookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
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