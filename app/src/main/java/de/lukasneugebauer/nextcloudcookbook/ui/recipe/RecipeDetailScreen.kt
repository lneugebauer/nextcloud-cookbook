package de.lukasneugebauer.nextcloudcookbook.ui.recipe

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.ui.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.ui.components.Loader
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NcBlue

@Composable
fun RecipeScreen(
    navController: NavHostController,
    recipeId: Int?,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state is RecipeDetailScreenState.Loaded) {
                        Text(
                            text = state.data.name,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = NcBlue,
                contentColor = Color.White
            )
        }
    ) {
        when (state) {
            is RecipeDetailScreenState.Initial -> {
                if (recipeId == null) {
                    Text(text = "Error: Couldn't load recipe.")
                } else {
                    viewModel.getRecipe(recipeId)
                    Loader()
                }
            }
            is RecipeDetailScreenState.Loaded -> {
                val recipe = state.data
                Column(
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.padding_l))
                        .verticalScroll(
                            rememberScrollState(),
                            flingBehavior = ScrollableDefaults.flingBehavior()
                        )
                ) {
                    AuthorizedImage(
                        imageUrl = recipe.imageUrl,
                        contentDescription = recipe.name,
                        modifier = Modifier
                            .aspectRatio(16f / 9f)
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(id = R.dimen.padding_l))
                    )
                    Text(
                        text = recipe.name,
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                        style = MaterialTheme.typography.h5
                    )
                    if (recipe.description != "") {
                        Text(
                            text = recipe.description,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if (recipe.ingredients.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_ingredients),
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                            style = MaterialTheme.typography.h6
                        )
                        recipe.ingredients.forEachIndexed { index, ingredient ->
                            val paddingBottom =
                                if (recipe.ingredients.size == index + 1) {
                                    dimensionResource(id = R.dimen.padding_l)
                                } else {
                                    dimensionResource(id = R.dimen.padding_xs)
                                }
                            Text(
                                text = ingredient,
                                modifier = Modifier
                                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                    .padding(
                                        top = dimensionResource(id = R.dimen.padding_xs),
                                        bottom = paddingBottom
                                    ),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                    if (recipe.tools.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_tools),
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                            style = MaterialTheme.typography.h6
                        )
                        val tools = recipe.tools.joinToString(separator = ", ")
                        Text(
                            text = tools,
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
                            style = MaterialTheme.typography.body1
                        )
                    }
                    if (recipe.instructions.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_instructions),
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                            style = MaterialTheme.typography.h6
                        )
                        recipe.instructions.forEachIndexed { index, instruction ->
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(id = R.dimen.padding_m),
                                    vertical = dimensionResource(id = R.dimen.padding_xs)
                                )
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
                                Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_s)))
                                Text(
                                    text = instruction,
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .fillMaxHeight(),
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}