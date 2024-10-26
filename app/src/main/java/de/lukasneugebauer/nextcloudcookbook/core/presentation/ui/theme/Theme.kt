package de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette =
    darkColorScheme(
        primary = NcBlue700,
        primaryContainer = NcBlue800,
        secondary = NcBlue700,
        secondaryContainer = NcBlue800,
        background = Color.Black,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
    )

private val LightColorPalette =
    lightColorScheme(
        primary = NcBlue700,
        primaryContainer = NcBlue800,
        secondary = NcBlue700,
        secondaryContainer = NcBlue800,
        background = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
    )

@Composable
fun NextcloudCookbookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colors =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (isSystemInDarkTheme()) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }
            else -> {
                if (darkTheme) {
                    DarkColorPalette
                } else {
                    LightColorPalette
                }
            }
        }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
