package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.cooking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.colintheshots.twain.MarkdownText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.KeepScreenOn
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.CookingModeState
import kotlinx.coroutines.launch

private val StepButtonMinHeight = 64.dp

/**
 * Shows a recipe's instructions one step at a time, with large buttons to move between steps, and
 * keeps the screen on while it is open.
 */
@Destination<MainGraph>
@Composable
fun CookingModeScreen(
    navigator: DestinationsNavigator,
    @Suppress("UNUSED_PARAMETER") recipeId: String,
    viewModel: CookingModeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    KeepScreenOn()
    HideBottomNavigation()

    CookingModeLayout(
        state = state,
        onClose = { navigator.popBackStack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeLayout(
    state: CookingModeState,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.recipeName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.recipe_cooking_mode_close),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            when {
                // Keep showing the steps if a background refresh fails or starts loading again.
                state.instructions.isNotEmpty() ->
                    CookingSteps(instructions = state.instructions, onFinish = onClose)

                state.loading -> Loader()

                state.error != null -> CenteredMessage(text = state.error.asString())

                else -> CenteredMessage(text = stringResource(R.string.recipe_cooking_mode_no_instructions))
            }
        }
    }
}

@Composable
private fun CookingSteps(
    instructions: List<Instruction>,
    onFinish: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { instructions.size })
    val scope = rememberCoroutineScope()
    val currentStep = pagerState.currentPage.coerceIn(0, instructions.lastIndex)
    val isFirstStep = currentStep == 0
    val isLastStep = currentStep == instructions.lastIndex

    Column(modifier = Modifier.fillMaxSize()) {
        StepProgress(currentStep = currentStep, stepCount = instructions.size)
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) { page ->
            Step(instruction = instructions[page])
        }
        StepControls(
            isFirstStep = isFirstStep,
            isLastStep = isLastStep,
            onPrevious = {
                scope.launch { pagerState.animateScrollToPage(currentStep - 1) }
            },
            onNext = {
                if (isLastStep) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(currentStep + 1) }
                }
            },
        )
    }
}

@Composable
private fun StepProgress(
    currentStep: Int,
    stepCount: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_m))
                .padding(bottom = dimensionResource(R.dimen.padding_s)),
    ) {
        Text(
            text = stringResource(R.string.recipe_cooking_mode_step, currentStep + 1, stepCount),
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_s)),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / stepCount },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Step(instruction: Instruction) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.padding_m)),
    ) {
        with(LocalDensity.current) {
            val textStyle = MaterialTheme.typography.headlineSmall
            MarkdownText(
                markdown = instruction.value,
                modifier = Modifier.fillMaxWidth(),
                fontSize = textStyle.fontSize * this.fontScale,
                style = textStyle.copy(color = LocalContentColor.current),
            )
        }
    }
}

@Composable
private fun StepControls(
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        OutlinedButton(
            onClick = onPrevious,
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = StepButtonMinHeight),
            enabled = !isFirstStep,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = stringResource(R.string.recipe_cooking_mode_previous_step),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Button(
            onClick = onNext,
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = StepButtonMinHeight),
        ) {
            if (isLastStep) {
                Text(
                    text = stringResource(R.string.recipe_cooking_mode_finish),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            } else {
                Text(
                    text = stringResource(R.string.recipe_cooking_mode_next_step),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_m)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun CookingModeLayoutPreview() {
    NextcloudCookbookTheme {
        CookingModeLayout(
            state =
                CookingModeState(
                    recipeName = "Lorem ipsum",
                    instructions =
                        List(3) {
                            Instruction(
                                id = it,
                                value =
                                    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                                        "eirmod tempor invidunt ut labore et dolore magna aliquyam erat.",
                            )
                        },
                    loading = false,
                ),
            onClose = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun CookingModeLayoutNoInstructionsPreview() {
    NextcloudCookbookTheme {
        CookingModeLayout(
            state = CookingModeState(recipeName = "Lorem ipsum", loading = false),
            onClose = {},
        )
    }
}
