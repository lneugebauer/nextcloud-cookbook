package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.pluralResource
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.getActivity
import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeEditScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListWithArgumentsScreenDestination
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Nutrition
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipe
import dev.jeziellago.compose.markdowntext.MarkdownText
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
                onNavIconClick = { navigator.navigateUp() },
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
                shareText =
                    viewModel.getShareText(
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
                        ingredientsTitle =
                            pluralResource(
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
            FloatingActionButton(
                onClick = {
                    if (recipe.isNotEmpty()) {
                        navigator.navigate(RecipeEditScreenDestination(recipe.id))
                    }
                },
                shape = MaterialTheme.shapes.medium,
            ) {
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
                modifier =
                    Modifier
                        .padding(paddingValues = innerPadding)
                        .verticalScroll(rememberScrollState()),
                calculatedIngredients = state.calculatedIngredients,
                currentYield = state.currentYield,
                onDecreaseYield = {
                    viewModel.decreaseYield()
                },
                onIncreaseYield = {
                    viewModel.increaseYield()
                },
                onKeywordClick = {
                    navigator.navigate(
                        RecipeListWithArgumentsScreenDestination(
                            categoryName = null,
                            keyword = it,
                        ),
                    )
                },
                onResetYield = {
                    viewModel.resetYield()
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

@OptIn(ExperimentalMaterial3Api::class)
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
        val sendIntent: Intent =
            Intent().apply {
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
                    Icons.AutoMirrored.Filled.ArrowBack,
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
    )
}

@Composable
private fun DropDownMenuItemOpenSource(
    context: Context,
    recipeUrl: String,
) {
    if (recipeUrl.isNotBlank()) {
        DropdownMenuItem(
            onClick = { Uri.parse(recipeUrl).openInBrowser(context) },
            text = { Text(stringResource(id = R.string.recipe_more_menu_share)) },
        )
    }
}

@Composable
private fun DropDownMenuItemEdit(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = { Text(text = stringResource(id = R.string.recipe_more_menu_edit)) },
    )
}

@Composable
private fun DropDownMenuItemDelete(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = { Text(text = stringResource(id = R.string.recipe_more_menu_delete)) },
    )
}

@Composable
private fun Content(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    calculatedIngredients: List<String>,
    currentYield: Int,
    onDecreaseYield: () -> Unit,
    onIncreaseYield: () -> Unit,
    onKeywordClick: (keyword: String) -> Unit,
    onResetYield: () -> Unit,
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
            Ingredients(
                calculatedIngredients.ifEmpty { recipe.ingredients },
                onDecreaseYield,
                onIncreaseYield,
                onResetYield,
                currentYield,
                recipe.yield != currentYield,
            )
        }
        if (recipe.nutrition != null) {
            Nutrition(recipe.nutrition)
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
private fun Image(
    imageUrl: String,
    name: String,
) {
    AuthorizedImage(
        imageUrl = imageUrl,
        contentDescription = name,
        modifier =
            Modifier
                .aspectRatio(4f / 3f)
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
    )
}

@Composable
private fun Name(name: String) {
    Text(
        text = name,
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Keywords(
    keywords: List<String>,
    onClick: (keyword: String) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
    ) {
        keywords.forEach {
            AssistChip(
                onClick = { onClick.invoke(it) },
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        labelColor = MaterialTheme.colorScheme.onSecondary,
                        // disabledContainerColor = MaterialTheme.colorScheme.surface,
                        // selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                label = { Text(text = it) },
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Composable
private fun Description(description: String) {
    MarkdownText(
        markdown = description,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
    )
}

@Composable
private fun Meta(
    prepTime: Duration?,
    cookTime: Duration?,
    totalTime: Duration?,
) {
    Row(
        modifier =
            Modifier
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
private fun RowScope.MetaBox(
    icon: ImageVector,
    duration: Long,
    @StringRes text: Int,
) {
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
        modifier =
            Modifier
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
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Ingredients(
    ingredients: List<String>,
    onDecreaseYield: () -> Unit,
    onIncreaseYield: () -> Unit,
    onResetYield: () -> Unit,
    currentYield: Int,
    showResetButton: Boolean,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.recipe_ingredients),
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                    .weight(1f),
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = {
            clipboardManager.setText(
                buildAnnotatedString {
                    ingredients.joinTo(this, separator = "\n- ", prefix = "- ")
                    toAnnotatedString()
                },
            )
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                Toast.makeText(
                    context,
                    context.resources.getQuantityString(R.plurals.recipe_ingredients_copied, ingredients.size),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }) {
            Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.recipe_ingredients_copy))
        }
    }
    Row(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onDecreaseYield,
            enabled = currentYield > 1,
        ) {
            Icon(imageVector = Icons.Outlined.Remove, contentDescription = stringResource(id = R.string.recipe_servings_decrease))
        }
        Text(
            text = pluralResource(resId = R.plurals.recipe_servings, currentYield, currentYield),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onIncreaseYield) {
            Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(id = R.string.recipe_servings_increase))
        }
        if (showResetButton) {
            Button(onClick = onResetYield) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = stringResource(id = R.string.recipe_servings_reset))
            }
        }
    }
    ingredients.forEach { ingredient ->
        var checked by rememberSaveable { mutableStateOf(false) }
        MarkdownText(
            markdown =
                if (checked) {
                    "~~$ingredient~~"
                } else {
                    ingredient
                },
            modifier =
                Modifier
                    .combinedClickable(
                        onLongClick = {
                            clipboardManager.setText(
                                buildAnnotatedString {
                                    append(ingredient)
                                    toAnnotatedString()
                                },
                            )
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                Toast.makeText(
                                    context,
                                    context.resources.getQuantityString(R.plurals.recipe_ingredients_copied, 1),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        onClick = {
                            checked = !checked
                        },
                    )
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        )
    }
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_m)))
}

@Composable
private fun NutritionItem(
    @StringRes label: Int,
    value: String?,
) {
    if (value != null) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${stringResource(label)} ")
                }
                append(value)
            },
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(
                        top = dimensionResource(id = R.dimen.padding_xs),
                        bottom = dimensionResource(id = R.dimen.padding_xs),
                    ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Nutrition(nutrition: Nutrition) {
    Text(
        text = stringResource(R.string.recipe_nutrition),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_calories,
        value = nutrition.calories,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_carbohydrate,
        value = nutrition.carbohydrateContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_cholesterol,
        value = nutrition.cholesterolContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_fat_total,
        value = nutrition.fatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_fiber,
        value = nutrition.fiberContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_protein,
        value = nutrition.proteinContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_saturated_fat,
        value = nutrition.saturatedFatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_serving_size,
        value = nutrition.servingSize,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_sodium,
        value = nutrition.sodiumContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_sugar,
        value = nutrition.sugarContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_trans_fat,
        value = nutrition.transFatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_unsaturated_fat,
        value = nutrition.unsaturatedFatContent,
    )
    val spacingBottom =
        dimensionResource(id = R.dimen.padding_l) - dimensionResource(id = R.dimen.padding_xs)
    Spacer(modifier = Modifier.size(spacingBottom))
}

@Composable
private fun Tools(tools: List<String>) {
    Text(
        text = stringResource(R.string.recipe_tools),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
    MarkdownText(
        markdown = tools.joinToString(separator = ", "),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
    )
}

@Composable
private fun Instructions(instructions: List<String>) {
    val enabledStates = remember(instructions) { instructions.map { mutableStateOf(false) } }
    Text(
        text = stringResource(R.string.recipe_instructions),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        style = MaterialTheme.typography.titleLarge,
    )
    instructions.forEachIndexed { index, instruction ->
        Row(
            modifier =
                Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_s),
                        end = dimensionResource(id = R.dimen.padding_m),
                        bottom = dimensionResource(id = R.dimen.padding_xs),
                    ),
        ) {
            AssistChip(
                onClick = {
                    enabledStates[index].value = !enabledStates[index].value
                },
                modifier =
                    Modifier
                        .padding(end = dimensionResource(id = R.dimen.padding_xs)),
                enabled = true,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (enabledStates[index].value) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        labelColor =
                            if (enabledStates[index].value) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    ),
                label = {
                    Text(
                        text = "${index + 1}",
                        color =
                            if (enabledStates[index].value) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                },
            )
            MarkdownText(
                markdown = instruction,
                modifier =
                    Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                        .fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeywordsPreview() {
    val keywords =
        List(5) {
            "Keyword ${it + 1}"
        }
    NextcloudCookbookTheme {
        Keywords(keywords = keywords, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InstructionsPreview() {
    val instructions =
        List(5) {
            val loremIpsum =
                """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam
                |nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                |voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd
                |gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum
                |dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
                |ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
                |et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
                |sanctus est Lorem ipsum dolor sit amet.
                """.trimMargin()
            val loremIpsumList = loremIpsum.split(" ")
            val stringArray = loremIpsumList.take((it + 1) * (it + 1))
            stringArray.joinToString(" ")
        }
    NextcloudCookbookTheme {
        Column {
            Instructions(instructions = instructions)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() {
    val recipe =
        Recipe(
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
            tools =
                List(1) {
                    "Lorem ipsum"
                },
            ingredients =
                List(2) {
                    "Lorem ipsum"
                },
            instructions =
                List(1) {
                    """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                        |eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                        |voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
                        |clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
                        |amet.
                    """.trimMargin()
                },
            createdAt = "",
            modifiedAt = "",
        )
    NextcloudCookbookTheme {
        Content(
            recipe = recipe,
            calculatedIngredients = emptyList(),
            currentYield = 2,
            onDecreaseYield = {},
            onIncreaseYield = {},
            onKeywordClick = {},
            onResetYield = {},
        )
    }
}
