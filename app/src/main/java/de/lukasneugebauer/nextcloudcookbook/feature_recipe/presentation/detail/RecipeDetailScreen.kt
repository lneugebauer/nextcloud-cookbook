package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorized_image.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.pluralResource
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.util.emptyRecipe
import java.time.Duration

@ExperimentalCoilApi
@Composable
fun RecipeDetailScreen(
    navController: NavHostController,
    recipeId: Int?,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var recipe: Recipe by remember { mutableStateOf(emptyRecipe()) }

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
            RecipeDetailContent(
                recipe = recipe,
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

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
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                shareRecipe()
            }) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = stringResource(id = R.string.common_share)
                )
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@ExperimentalCoilApi
@Composable
fun RecipeDetailContent(recipe: Recipe, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        RecipeDetailImage(recipe.imageUrl, recipe.name)
        RecipeDetailName(recipe.name)
        if (recipe.description != "") {
            RecipeDetailDescription(recipe.description)
        }
        RecipeDetailMeta(recipe.prepTime, recipe.cookTime, recipe.totalTime)
        if (recipe.ingredients.isNotEmpty()) {
            RecipeDetailIngredients(recipe.ingredients, recipe.yield)
        }
        if (recipe.tools.isNotEmpty()) {
            RecipeDetailTools(recipe.tools)
        }
        if (recipe.instructions.isNotEmpty()) {
            RecipeDetailInstructions(recipe.instructions)
        }
    }
}

@ExperimentalCoilApi
@Composable
fun RecipeDetailImage(imageUrl: String, name: String) {
    AuthorizedImage(
        imageUrl = imageUrl,
        contentDescription = name,
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_l))
    )
}

@Composable
fun RecipeDetailName(name: String) {
    Text(
        text = name,
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h5
    )
}

@Composable
fun RecipeDetailDescription(description: String) {
    Text(
        text = description,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun RecipeDetailMeta(prepTime: Duration?, cookTime: Duration?, totalTime: Duration?) {
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l))
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (prepTime != null) {
            RecipeDetailMetaBox(duration = prepTime.toMinutes(), text = R.string.recipe_prep_time)
        }
        if (cookTime != null) {
            RecipeDetailMetaBox(duration = cookTime.toMinutes(), text = R.string.recipe_cook_time)
        }
        if (totalTime != null) {
            RecipeDetailMetaBox(duration = totalTime.toMinutes(), text = R.string.recipe_total_time)
        }
    }
}

@Composable
fun RowScope.RecipeDetailMetaBox(duration: Long, @StringRes text: Int) {
    Box(modifier = Modifier.weight(1f)) {
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Timer, contentDescription = "")
            Text(text = stringResource(id = R.string.recipe_duration, duration), fontWeight = FontWeight.Bold)
            Text(
                text = stringResource(id = text),
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle.Default.copy(textAlign = TextAlign.Center)
            )
        }
    }
}

@Composable
fun RecipeDetailIngredients(ingredients: List<String>, servings: Int) {
    Text(
        text = pluralResource(R.plurals.recipe_ingredients_servings, servings, servings),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h6
    )
    ingredients.forEachIndexed { index, ingredient ->
        val paddingBottom =
            if (ingredients.size == index + 1) {
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

@Composable
fun RecipeDetailTools(tools: List<String>) {
    Text(
        text = stringResource(R.string.recipe_tools),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h6
    )
    val tools = tools.joinToString(separator = ", ")
    Text(
        text = tools,
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun RecipeDetailInstructions(instructions: List<String>) {
    Text(
        text = stringResource(R.string.recipe_instructions),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h6
    )
    instructions.forEachIndexed { index, instruction ->
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