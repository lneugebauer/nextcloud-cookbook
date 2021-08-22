package de.lukasneugebauer.nextcloudcookbook.features.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.data.Recipe
import okhttp3.Credentials

@Composable
fun Recipe(
    recipe: Recipe
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = rememberImagePainter(
                data = recipe.imageUrl,
                builder = {
                    val credentials =
                        Credentials.basic(BuildConfig.NC_USERNAME, BuildConfig.NC_APP_PASSWORD)
                    addHeader("Authorization", credentials)
                    crossfade(750)
                }
            ),
            contentDescription = recipe.name,
            modifier = Modifier
                .wrapContentHeight()
                .aspectRatio(16f / 9f)
                .clipToBounds()
                .fillMaxWidth()
        )
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.h5
        )
        if (recipe.description.isNotBlank()) {
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.body1
            )
        }
        if (recipe.tools.isNotEmpty()) {
            Text(
                text = "Utensilien",
                style = MaterialTheme.typography.h6
            )
            recipe.tools.forEach { tool ->
                Text(
                    text = "- $tool",
                    style = MaterialTheme.typography.body1
                )
            }
        }
        if (recipe.ingredients.isNotEmpty()) {
            Text(
                text = "Zutaten",
                style = MaterialTheme.typography.h6
            )
            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "- $ingredient",
                    style = MaterialTheme.typography.body1
                )
            }
        }
        if (recipe.instructions.isNotEmpty()) {
            Text(
                text = "Zubereitung",
                style = MaterialTheme.typography.h6
            )
            recipe.instructions.forEachIndexed { index, instruction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(all = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = (index + 1).toString())
                        }
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    }
}