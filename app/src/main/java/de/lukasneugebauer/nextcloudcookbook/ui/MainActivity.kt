package de.lukasneugebauer.nextcloudcookbook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.*
import de.lukasneugebauer.nextcloudcookbook.data.RecipeRepository
import de.lukasneugebauer.nextcloudcookbook.ui.categories.CategoriesScreen
import de.lukasneugebauer.nextcloudcookbook.ui.categories.CategoriesViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.ui.components.TopBar
import de.lukasneugebauer.nextcloudcookbook.ui.home.HomeScreen
import de.lukasneugebauer.nextcloudcookbook.ui.home.HomeViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.recipe.RecipeDetailViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.recipe.RecipeScreen
import de.lukasneugebauer.nextcloudcookbook.ui.recipes.RecipesScreen
import de.lukasneugebauer.nextcloudcookbook.ui.recipes.RecipesViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.search.SearchScreen
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NextcloudCookbookTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var recipeRepository: RecipeRepository

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextcloudCookbookApp()
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookApp() {
    NextcloudCookbookTheme {
        val allScreens = values().toList()
        val navController = rememberNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen =
            NextcloudCookbookScreen.fromRoute(backstackEntry.value?.destination?.route)

        Scaffold(
            topBar = { TopBar(currentScreen = currentScreen) },
            bottomBar = {
                BottomBar(
                    allScreens = allScreens,
                    navController = navController,
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            NextcloudCookbookNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, startDestination = Home.name, modifier = modifier) {
        composable(Home.name) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(navController, homeViewModel)
        }
        composable(Categories.name) {
            val categoriesViewModel = hiltViewModel<CategoriesViewModel>()
            CategoriesScreen(categoriesViewModel)
        }
        composable(Recipes.name) {
            val recipesViewModel = hiltViewModel<RecipesViewModel>()
            RecipesScreen(navController, recipesViewModel)
        }
        composable(
            route = "${Recipe.name}/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeDetailViewModel = hiltViewModel<RecipeDetailViewModel>()
            RecipeScreen(recipeDetailViewModel, backStackEntry.arguments?.getInt("recipeId"))
        }
        composable(Search.name) {
            SearchScreen()
        }
    }
}