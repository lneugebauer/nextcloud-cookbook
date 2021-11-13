package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue

@Composable
fun RecipeDetailTopBar(recipe: Recipe, onNavIconClick: () -> Unit, shareText: String) {
    val context = LocalContext.current

    fun shareRecipe() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, recipe.name)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    TopAppBar(
        title = {
            Text(
                text = recipe.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                shareRecipe()
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@Composable
fun RecipeDetailScreen(
    navController: NavHostController,
    recipeId: Int?,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    fun getEmptyRecipe(): Recipe = Recipe(
        id = 0,
        name = "",
        description = "",
        url = "",
        imageUrl = "",
        category = "",
        keywords = emptyList(),
        yield = 0,
        prepTime = null,
        cookTime = null,
        totalTime = null,
        nutrition = null,
        tools = emptyList(),
        ingredients = emptyList(),
        instructions = emptyList(),
        createdAt = "",
        modifiedAt = ""
    )

    val state = viewModel.state.value
    var recipe: Recipe by remember { mutableStateOf(getEmptyRecipe()) }

    Scaffold(
        topBar = {
            RecipeDetailTopBar(
                recipe = recipe,
                onNavIconClick = { navController.popBackStack() },
                shareText = viewModel.getShareText()
            )
        }
    ) { innerPadding ->
        if (recipeId != null && state.data == null && state.error == null && state.loading) {
            viewModel.getRecipe(recipeId)
        }
        if (state.loading) {
            Loader()
        }
        if (state.error != null && !state.loading) {
            Text(text = state.error)
        }
        if (state.data != null && state.error == null && !state.loading) {
            recipe = state.data
            Column(
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .verticalScroll(rememberScrollState())
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
                                    .border(
                                        2.dp,
                                        MaterialTheme.colors.onBackground,
                                        CircleShape
                                    ),
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