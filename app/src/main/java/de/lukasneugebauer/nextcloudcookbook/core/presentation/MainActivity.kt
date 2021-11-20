package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.api.NextcloudAPI.ApiConnectedListener
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.ui.UiExceptionManager
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.*
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.launch.LaunchScreen
import de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.login.LoginScreen
import de.lukasneugebauer.nextcloudcookbook.feature_category.presentation.list.CategoriesScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.detail.RecipeDetailScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.home.HomeScreen
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.list.RecipeListScreen
import de.lukasneugebauer.nextcloudcookbook.feature_search.presentation.search.SearchScreen
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var api: ApiProvider
    private val viewModel: MainViewModel by viewModels()

    @ExperimentalCoilApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NextcloudCookbookApp(onSsoClick = {
                openAccountChooser()
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            AccountImporter.onActivityResult(
                requestCode,
                resultCode,
                data,
                this
            ) { ssoAccount ->
                viewModel.storeNcSingleSignOnAccount(ssoAccount)

                SingleAccountHelper.setCurrentAccount(applicationContext, ssoAccount.name)

                api.initApi(object : ApiConnectedListener {
                    override fun onConnected() {}
                    override fun onError(ex: java.lang.Exception) {}
                })
            }
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun openAccountChooser() {
        try {
            AccountImporter.pickNewAccount(this)
        } catch (e: NextcloudFilesAppNotInstalledException) {
            UiExceptionManager.showDialogForException(this, e)
        } catch (e: AndroidGetAccountsPermissionNotGranted) {
            UiExceptionManager.showDialogForException(this, e)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookApp(onSsoClick: () -> Unit) {
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
                navController = navController,
                modifier = Modifier.padding(paddingValues = innerPadding),
                onSsoClick = onSsoClick
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookNavHost(
    navController: NavHostController,
    modifier: Modifier,
    onSsoClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = Launch.name, modifier = modifier) {
        composable(Launch.name) {
            LaunchScreen(navController)
        }
        composable(Login.name) {
            LoginScreen(navController, onSsoClick)
        }
        composable(Home.name) {
            HomeScreen(navController)
        }
        composable(Categories.name) {
            CategoriesScreen(navController)
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
                navController,
                backStackEntry.arguments?.getInt("recipeId")
            )
        }
        composable(Search.name) {
            SearchScreen()
        }
    }
}