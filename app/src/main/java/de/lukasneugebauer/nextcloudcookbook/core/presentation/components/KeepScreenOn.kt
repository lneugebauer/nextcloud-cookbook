package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.getActivity

/**
 * Keeps the screen on for as long as this composable is part of the composition.
 *
 * Callers are reference counted on the window's decor view, so the flag is only cleared once the
 * last caller leaves. That matters during navigation: when one screen that keeps the screen on
 * opens another one, the new screen enters the composition before the old one leaves it, and a
 * naive implementation would clear the flag while the new screen is still showing.
 */
@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.getActivity()?.window ?: return@DisposableEffect onDispose {}
        val decorView = window.decorView
        val holders = (decorView.getTag(R.id.keep_screen_on_holders) as? Int ?: 0) + 1
        decorView.setTag(R.id.keep_screen_on_holders, holders)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            val remaining =
                ((decorView.getTag(R.id.keep_screen_on_holders) as? Int ?: 1) - 1).coerceAtLeast(0)
            decorView.setTag(R.id.keep_screen_on_holders, remaining)
            if (remaining == 0) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}
