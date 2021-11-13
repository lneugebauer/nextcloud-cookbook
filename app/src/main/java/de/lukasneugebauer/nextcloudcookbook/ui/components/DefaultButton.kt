package de.lukasneugebauer.nextcloudcookbook.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NcBlue

// TODO: 05.10.21 Finalize styling of button
@Composable
fun DefaultButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = NcBlue
        ),
        content = content
    )
}

@Preview
@Composable
fun DefaultButtonPreview() {
    DefaultButton(onClick = { }) {
        Text("Button")
    }
}