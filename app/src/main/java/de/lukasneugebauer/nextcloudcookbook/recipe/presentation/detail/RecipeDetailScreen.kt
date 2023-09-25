package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.pluralResource
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.getActivity
import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeEditScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListScreenDestination
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components.CircleChip
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipe
import java.time.Duration

@Destination
@Composable
fun RecipeDetailScreen(
    navigator: DestinationsNavigator,
    @Suppress("UNUSED_PARAMETER") recipeId: Int,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val recipe by remember { derivedStateOf { state.data ?: emptyRecipe() } }
    val errorMessage by remember { derivedStateOf { state.error } }

    if (viewModel.stayAwake) {
        KeepScreenOn()
    }

    if (state.deleted) {
        LaunchedEffect(state) {
            navigator.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                recipe = recipe,
                onNavIconClick = { navigator.popBackStack() },
                onEditClick = {
                    if (recipe.isNotEmpty()) {
                        navigator.navigate(RecipeEditScreenDestination(recipe.id))
                    }
                },
                onDeleteClick = {
                    if (recipe.isNotEmpty()) {
                        viewModel.deleteRecipe(
                            recipe.id,
                            recipe.category,
                        )
                    }
                },
                shareText = viewModel.getShareText(
                    sourceTitle = stringResource(id = R.string.recipe_source),
                    prepTime = { duration ->
                        context.getString(R.string.recipe_prep_time)
                            .plus(": ")
                            .plus(context.getString(R.string.recipe_duration, duration))
                    },
                    cookTime = { duration ->
                        context.getString(R.string.recipe_cook_time)
                            .plus(": ")
                            .plus(context.getString(R.string.recipe_duration, duration))
                    },
                    totalTime = { duration ->
                        context.getString(R.string.recipe_total_time)
                            .plus(": ")
                            .plus(context.getString(R.string.recipe_duration, duration))
                    },
                    ingredientsTitle = pluralResource(
                        R.plurals.recipe_ingredients_servings,
                        recipe.yield,
                        recipe.yield,
                    ),
                    toolsTitle = stringResource(id = R.string.recipe_tools),
                    instructionsTitle = stringResource(id = R.string.recipe_instructions),
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (recipe.isNotEmpty()) {
                    navigator.navigate(RecipeEditScreenDestination(recipe.id))
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.common_edit),
                )
            }
        },
    ) { innerPadding ->
        if (state.loading) {
            Loader()
        }
        errorMessage?.let {
            if (!state.loading) {
                Text(text = it.asString())
            }
        }
        if (recipe.isNotEmpty() && state.error == null && !state.loading) {
            Content(
                recipe = recipe,
                modifier = Modifier
                    .padding(paddingValues = innerPadding)
                    .verticalScroll(rememberScrollState()),
                onKeywordClick = {
                    navigator.navigate(
                        RecipeListScreenDestination(
                            categoryName = null,
                            keyword = it,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    val activity = context.getActivity()
    val window = activity?.window
    DisposableEffect(Unit) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun TopBar(
    recipe: Recipe,
    onNavIconClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    shareText: String,
) {
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
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
        actions = {
            IconButton(onClick = {
                shareRecipe()
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.common_share),
                )
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.common_more),
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropDownMenuItemOpenSource(context, recipe.url)
                    DropDownMenuItemEdit(onEditClick)
                    DropDownMenuItemDelete(onDeleteClick)
                }
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White,
    )
}

@Composable
private fun DropDownMenuItemOpenSource(context: Context, recipeUrl: String) {
    if (recipeUrl.isNotBlank()) {
        DropdownMenuItem(onClick = { Uri.parse(recipeUrl).openInBrowser(context) }) {
            Text(text = stringResource(id = R.string.recipe_more_menu_share))
        }
    }
}

@Composable
private fun DropDownMenuItemEdit(onClick: () -> Unit) {
    DropdownMenuItem(onClick) {
        Text(text = stringResource(id = R.string.recipe_more_menu_edit))
    }
}

@Composable
private fun DropDownMenuItemDelete(onClick: () -> Unit) {
    DropdownMenuItem(onClick = onClick) {
        Text(text = stringResource(id = R.string.recipe_more_menu_delete))
    }
}

@Composable
private fun Content(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onKeywordClick: (keyword: String) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Image(recipe.imageUrl, recipe.name)
        Name(recipe.name)
        if (recipe.keywords.isNotEmpty()) {
            Keywords(keywords = recipe.keywords, onClick = onKeywordClick)
        }
        if (recipe.description.isNotBlank()) {
            Description(recipe.description)
        }
        if (recipe.prepTime?.notZero() == true ||
            recipe.cookTime?.notZero() == true ||
            recipe.totalTime?.notZero() == true
        ) {
            Meta(recipe.prepTime, recipe.cookTime, recipe.totalTime)
        }
        if (recipe.category.isNotEmpty()) {
            Category(category = recipe.category)
        }
        if (recipe.ingredients.isNotEmpty()) {
            Ingredients(recipe.ingredients, recipe.yield)
        }
        if (recipe.tools.isNotEmpty()) {
            Tools(recipe.tools)
        }
        if (recipe.instructions.isNotEmpty()) {
            Instructions(recipe.instructions)
        }
        Gap(size = dimensionResource(id = R.dimen.padding_s))
        Gap(size = dimensionResource(id = R.dimen.fab_offset))
    }
}

@Composable
private fun Image(imageUrl: String, name: String) {
    AuthorizedImage(
        imageUrl = imageUrl,
        contentDescription = name,
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
    )
}

@Composable
private fun Name(name: String) {
    Text(
        text = name,
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h5,
    )
}

@Composable
private fun Keywords(keywords: List<String>, onClick: (keyword: String) -> Unit) {
    FlowRow(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        mainAxisSpacing = dimensionResource(id = R.dimen.padding_s),
        crossAxisSpacing = dimensionResource(id = R.dimen.padding_s),
    ) {
        keywords.forEach {
            Chip(
                onClick = { onClick.invoke(it) },
                border = BorderStroke(2.dp, NcBlue700),
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color.Transparent,
                ),
            ) {
                Text(text = it)
            }
        }
    }
}

@Composable
private fun Description(description: String) {
    Text(
        text = description,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.body1,
    )
}

@Composable
private fun Meta(prepTime: Duration?, cookTime: Duration?, totalTime: Duration?) {
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(
                top = dimensionResource(id = R.dimen.padding_m),
                bottom = dimensionResource(id = R.dimen.padding_l),
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (prepTime != null && prepTime != Duration.ZERO) {
            // TODO: 18.11.21 Use prep icon
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = prepTime.toMinutes(),
                text = R.string.recipe_prep_time,
            )
        }
        if (cookTime != null && cookTime != Duration.ZERO) {
            // TODO: 18.11.21 Use cook icon
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = cookTime.toMinutes(),
                text = R.string.recipe_cook_time,
            )
        }
        if (totalTime != null && totalTime != Duration.ZERO) {
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = totalTime.toMinutes(),
                text = R.string.recipe_total_time,
            )
        }
    }
}

@Composable
private fun RowScope.MetaBox(icon: ImageVector, duration: Long, @StringRes text: Int) {
    Box(modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = "")
            Text(
                text = stringResource(id = R.string.recipe_duration, duration),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = text),
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle.Default.copy(textAlign = TextAlign.Center),
            )
        }
    }
}

@Composable
private fun Category(category: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = stringResource(id = R.string.recipe_category),
        )
        Text(
            text = category,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
private fun Ingredients(ingredients: List<String>, servings: Int) {
    Text(
        text = pluralResource(R.plurals.recipe_ingredients_servings, servings, servings),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        style = MaterialTheme.typography.h6,
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
                    bottom = paddingBottom,
                ),
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
private fun Tools(tools: List<String>) {
    Text(
        text = stringResource(R.string.recipe_tools),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.h6,
    )
    Text(
        text = tools.joinToString(separator = ", "),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        style = MaterialTheme.typography.body1,
    )
}

@Composable
private fun Instructions(instructions: List<String>) {
    Text(
        text = stringResource(R.string.recipe_instructions),
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        style = MaterialTheme.typography.h6,
    )
    instructions.forEachIndexed { index, instruction ->
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(id = R.dimen.padding_m),
                vertical = dimensionResource(id = R.dimen.padding_s),
            ),
        ) {
            CircleChip(text = "${index + 1}", modifier = Modifier.weight(1f))
            Gap(size = dimensionResource(id = R.dimen.padding_s))
            Text(
                text = instruction,
                modifier = Modifier
                    .align(Alignment.Top)
                    .fillMaxHeight()
                    .weight(11f),
                style = MaterialTheme.typography.body1,
            )
        }
    }
}

@Preview
@Composable
private fun ContentPreview() {
    val recipe = Recipe(
        id = 1,
        name = "Lorem ipsum",
        description = "Lorem ipsum dolor sit amet",
        url = "https://www.example.com",
        imageOrigin = "https://www.example.com/image.jpg",
        imageUrl = "/apps/cookbook/recipes/1/image?size=full",
        category = "Lorem ipsum",
        keywords = emptyList(),
        yield = 2,
        prepTime = null,
        cookTime = Duration.parse("PT0H35M0S"),
        totalTime = Duration.parse("PT1H50M0S"),
        nutrition = null,
        tools = List(1) {
            "Lorem ipsum"
        },
        ingredients = List(2) {
            "Lorem ipsum"
        },
        instructions = List(1) {
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
        },
        createdAt = "",
        modifiedAt = "",
    )
    NextcloudCookbookTheme {
        Content(recipe = recipe, onKeywordClick = {})
    }
}
