package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.annotation.ExperimentalCoilApi
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.*
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.launch.LaunchScreen
import de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login.LoginScreen
import de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list.CategoryListScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail.RecipeDetailScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home.HomeScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list.RecipeListScreen
import de.lukasneugebauer.nextcloudcookbook.feature_search.presentation.search.SearchScreen
import de.lukasneugebauer.nextcloudcookbook.feature_settings.presentation.SettingsScreen
import javax.inject.Inject

@ExperimentalCoilApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NextcloudCookbookApp(
                preferencesManager = preferencesManager,
                sharedPreferences = sharedPreferences,
                window = this.window
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookApp(
    preferencesManager: PreferencesManager,
    sharedPreferences: SharedPreferences,
    window: Window
) {
    NextcloudCookbookTheme {
        val allScreens = values().toList()
        val navController = rememberNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen =
            NextcloudCookbookScreen.fromRoute(backstackEntry.value?.destination?.route)

        Scaffold(
            bottomBar = {
                if (currentScreen != Launch && currentScreen != Login) {
                    BottomBar(
                        allScreens = allScreens,
                        navController = navController,
                        currentScreen = currentScreen
                    )
                }
            }
        ) { innerPadding ->
            NextcloudCookbookNavHost(
                modifier = Modifier.padding(paddingValues = innerPadding),
                navController = navController,
                preferencesManager = preferencesManager,
                sharedPreferences = sharedPreferences,
                window = window
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookNavHost(
    modifier: Modifier,
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    sharedPreferences: SharedPreferences,
    window: Window
) {
    NavHost(navController = navController, startDestination = Launch.name, modifier = modifier) {
        composable(Launch.name) {
            LaunchScreen(navController)
        }
        composable(Login.name) {
            LoginScreen(navController)
        }
        composable(Home.name) {
            HomeScreen(navController)
        }
        composable(Categories.name) {
            CategoryListScreen(navController)
        }
        composable(
            "${Recipes.name}?categoryName={categoryName}",
            arguments = listOf(navArgument("categoryName") {
                nullable = true
                type = NavType.StringType
            })
        ) { backStackEntry ->
            RecipeListScreen(
                navController,
                backStackEntry.arguments?.getString("categoryName")
            )
        }
        composable(
            route = "${Recipe.name}?recipeId={recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            RecipeDetailScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                recipeId = backStackEntry.arguments?.getInt("recipeId"),
                window = window
            )
        }
        composable(Search.name) {
            SearchScreen()
        }
        composable(Settings.name) {
            SettingsScreen(navController, sharedPreferences)
        }
    }
}