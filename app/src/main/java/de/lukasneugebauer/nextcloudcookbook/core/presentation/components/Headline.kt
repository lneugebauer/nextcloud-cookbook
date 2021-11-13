package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Headline(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        style = MaterialTheme.typography.h5
    )
}