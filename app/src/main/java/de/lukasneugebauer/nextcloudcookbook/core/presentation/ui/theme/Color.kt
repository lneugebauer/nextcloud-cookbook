package de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val NcBlue900 = Color(0xFF005293)
val NcBlue800 = Color(0xFF0072B5)
val NcBlue700 = Color(0xFF0082C9)
val NcBlue600 = Color(0xFF0195DD)
val NcBlue500 = Color(0xFF02A3EB)
val NcBlue400 = Color(0xFF28B1EF)
val NcBlue300 = Color(0xFF4EBEF1)
val NcBlue200 = Color(0xFF7FD1F6)
val NcBlue100 = Color(0xFFB2E3F9)
val NcBlue50 = Color(0xFFE1F4FD)
val NcBlueGradient =
    Brush.linearGradient(
        listOf(
            NcBlue700,
            Color(0xFF1CAFFF),
        ),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )
