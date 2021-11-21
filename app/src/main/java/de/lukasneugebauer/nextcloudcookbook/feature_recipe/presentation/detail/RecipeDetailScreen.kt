package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.flowlayout.FlowRow
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
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
    var expanded by remember { mutableStateOf(false) }

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
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.common_more)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    RecipeDetailScreenDropDownMenuItemOpenSource(context, recipe.url)
                    RecipeDetailScreenDropDownMenuItemEdit(context)
                    RecipeDetailScreenDropDownMenuItemDelete(context)
                }
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}

@Composable
fun RecipeDetailScreenDropDownMenuItemOpenSource(context: Context, recipeUrl: String) {
    if (recipeUrl.isNotBlank()) {
        DropdownMenuItem(onClick = {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(recipeUrl)
            startActivity(context, openURL, null)
        }) {
            Text(text = stringResource(id = R.string.recipe_more_menu_share))
        }
    }
}

@Composable
fun RecipeDetailScreenDropDownMenuItemEdit(context: Context) {
    DropdownMenuItem(onClick = {
        Toast.makeText(
            context,
            "Function currently unavailable.",
            Toast.LENGTH_SHORT
        ).show()
    }) {
        Text(text = stringResource(id = R.string.recipe_more_menu_edit))
    }
}

@Composable
fun RecipeDetailScreenDropDownMenuItemDelete(context: Context) {
    DropdownMenuItem(onClick = {
        Toast.makeText(
            context,
            "Function currently unavailable.",
            Toast.LENGTH_SHORT
        ).show()
    }) {
        Text(text = stringResource(id = R.string.recipe_more_menu_delete))
    }
}

@ExperimentalCoilApi
@Composable
fun RecipeDetailContent(recipe: Recipe, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        RecipeDetailImage(recipe.imageUrl, recipe.name)
        RecipeDetailName(recipe.name)
        if (recipe.keywords.isNotEmpty()) {
            RecipeDetailKeywords(recipe.keywords)
        }
        if (recipe.description.isNotBlank()) {
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
        Gap(size = dimensionResource(id = R.dimen.padding_m))
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
            .padding(bottom = dimensionResource(id = R.dimen.padding_m))
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
fun RecipeDetailKeywords(keywords: List<String>) {
    FlowRow(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        mainAxisSpacing = dimensionResource(id = R.dimen.padding_s),
        crossAxisSpacing = dimensionResource(id = R.dimen.padding_s)
    ) {
        keywords.forEach {
            Box(
                modifier = Modifier
                    .border(width = 2.dp, color = NcBlue, shape = CircleShape)
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_s),
                        vertical = dimensionResource(id = R.dimen.padding_xs)
                    )
                    .height(dimensionResource(id = R.dimen.chip_height)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = it, style = MaterialTheme.typography.body2)
            }
        }
    }
}

@Composable
fun RecipeDetailDescription(description: String) {
    Text(
        text = description,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun RecipeDetailMeta(prepTime: Duration?, cookTime: Duration?, totalTime: Duration?) {
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(
                top = dimensionResource(id = R.dimen.padding_m),
                bottom = dimensionResource(id = R.dimen.padding_l)
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (prepTime != null) {
            // TODO: 18.11.21 Use prep icon
            RecipeDetailMetaBox(
                icon = Icons.Filled.Timer,
                duration = prepTime.toMinutes(),
                text = R.string.recipe_prep_time
            )
        }
        if (cookTime != null) {
            // TODO: 18.11.21 Use cook icon
            RecipeDetailMetaBox(
                icon = Icons.Filled.Timer,
                duration = cookTime.toMinutes(),
                text = R.string.recipe_cook_time
            )
        }
        if (totalTime != null) {
            RecipeDetailMetaBox(
                icon = Icons.Filled.Timer,
                duration = totalTime.toMinutes(),
                text = R.string.recipe_total_time
            )
        }
    }
}

@Composable
fun RowScope.RecipeDetailMetaBox(icon: ImageVector, duration: Long, @StringRes text: Int) {
    Box(modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = "")
            Text(
                text = stringResource(id = R.string.recipe_duration, duration),
                fontWeight = FontWeight.Bold
            )
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
            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
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
            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
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
                    .padding(top = dimensionResource(id = R.dimen.padding_xs))
                    .background(color = NcBlue, shape = CircleShape)
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_xs),
                        vertical = dimensionResource(id = R.dimen.padding_xs)
                    )
                    .size(size = dimensionResource(id = R.dimen.chip_height)),
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