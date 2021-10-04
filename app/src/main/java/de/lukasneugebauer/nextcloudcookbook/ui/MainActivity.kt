package de.lukasneugebauer.nextcloudcookbook.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.AccountImporter.IAccountAccessGranted
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.api.NextcloudAPI.ApiConnectedListener
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import com.nextcloud.android.sso.ui.UiExceptionManager
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen.*
import de.lukasneugebauer.nextcloudcookbook.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.ui.categories.CategoriesScreen
import de.lukasneugebauer.nextcloudcookbook.ui.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.ui.components.TopBar
import de.lukasneugebauer.nextcloudcookbook.ui.home.HomeScreen
import de.lukasneugebauer.nextcloudcookbook.ui.home.HomeViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.recipe.RecipeDetailViewModel
import de.lukasneugebauer.nextcloudcookbook.ui.recipe.RecipeScreen
import de.lukasneugebauer.nextcloudcookbook.ui.recipes.RecipesScreen
import de.lukasneugebauer.nextcloudcookbook.ui.search.SearchScreen
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            preferencesManager.preferencesFlow.distinctUntilChanged().collect { preferences ->
                Logger.d("preferences: $preferences", TAG)
                val nextcloudAccount = preferences.nextcloudAccount
                if (nextcloudAccount.name == "" || nextcloudAccount.username == "" || nextcloudAccount.token == "" || nextcloudAccount.url == "") {
                    openAccountChooser()
                }
            }
        }

        setContent {
            NextcloudCookbookApp()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AccountImporter.onActivityResult(
            requestCode,
            resultCode,
            data,
            this,
            object : IAccountAccessGranted {
                var callback: ApiConnectedListener = object : ApiConnectedListener {
                    override fun onConnected() {
                        // ignore this one… see 5)
                    }

                    override fun onError(ex: Exception) {
                        // TODO handle errors
                    }
                }

                override fun accountAccessGranted(account: SingleSignOnAccount) {
                    // As this library supports multiple accounts we created some helper methods if you only want to use one.
                    // The following line stores the selected account as the "default" account which can be queried by using
                    // the SingleAccountHelper.getCurrentSingleSignOnAccount(context) method
                    SingleAccountHelper.setCurrentAccount(applicationContext, account.name)

                    // Get the "default" account
                    var ssoAccount: SingleSignOnAccount? = null
                    try {
                        ssoAccount =
                            SingleAccountHelper.getCurrentSingleSignOnAccount(applicationContext)
                    } catch (e: NextcloudFilesAppAccountNotFoundException) {
                        UiExceptionManager.showDialogForException(applicationContext, e)
                    } catch (e: NoCurrentAccountSelectedException) {
                        UiExceptionManager.showDialogForException(applicationContext, e)
                    }
                    val nextcloudAPI = NextcloudAPI(
                        applicationContext,
                        ssoAccount!!, GsonBuilder().create(), callback
                    )

                    // TODO … (see code in section 4 and below)
                    Logger.d("name: ${ssoAccount.name}, username: ${ssoAccount.userId}, token: ${ssoAccount.token}, url: ${ssoAccount.url}", TAG)

                    lifecycleScope.launchWhenStarted {
                        val nextcloudAccount = NextcloudAccount(
                            name = ssoAccount.name,
                            username = ssoAccount.userId,
                            token = ssoAccount.token,
                            url = ssoAccount.url
                        )
                        preferencesManager.updateNextcloudAccount(nextcloudAccount)
                    }
                }
            })
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
            HomeScreen(navController)
        }
        composable(Categories.name) {
            CategoriesScreen()
        }
        composable(Recipes.name) {
            RecipesScreen(navController)
        }
        composable(
            route = "${Recipe.name}/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            RecipeScreen(backStackEntry.arguments?.getInt("recipeId"))
        }
        composable(Search.name) {
            SearchScreen()
        }
    }
}