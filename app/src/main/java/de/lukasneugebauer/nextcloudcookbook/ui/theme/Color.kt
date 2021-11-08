package de.lukasneugebauer.nextcloudcookbook.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val NcBlue = Color(0xFF0082C9)
val NcBlueGradient = Brush.linearGradient(
    listOf(
        Color(0xFF0082C9),
        Color(0xFF1CAFFF)
    ),
    start = Offset(0f, Float.POSITIVE_INFINITY),
    end= Offset(Float.POSITIVE_INFINITY, 0f)
)