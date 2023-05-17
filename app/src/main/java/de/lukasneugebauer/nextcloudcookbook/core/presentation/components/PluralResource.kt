package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun pluralResource(
    @PluralsRes resId: Int,
    quantity: Int,
    vararg formatArgs: Any? = emptyArray(),
): String {
    return LocalContext.current.resources.getQuantityString(resId, quantity, *formatArgs)
}
