package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.RecipeCreateScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.RecipeListWithArgumentsScreen(
    navigator: DestinationsNavigator,
    categoryName: String?,
    @Suppress("UNUSED_PARAMETER") keyword: String?,
    resultRecipient: ResultRecipient<RecipeCreateScreenDestination, String>,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    RecipeListScreenWrapper(
        navigator = navigator,
        categoryName = categoryName,
        resultRecipient = resultRecipient,
        viewModel = viewModel,
    )
}
