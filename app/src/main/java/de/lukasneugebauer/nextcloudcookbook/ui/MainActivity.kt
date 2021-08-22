package de.lukasneugebauer.nextcloudcookbook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.api.NextcloudApi
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import javax.inject.Inject

private const val TAG: String = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var nextcloudApi: NextcloudApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.v("Hello Nextcloud Cookbook!", TAG)
        lifecycleScope.launchWhenStarted {
            val recipes = nextcloudApi.getRecipes()
            Logger.d("recipes: $recipes")

            val id = recipes.first().recipe_id.toInt()
            val recipe = nextcloudApi.getRecipe(id)
            Logger.d("recipe: $recipe")
        }
        setContent {
            NextcloudCookbookTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NextcloudCookbookTheme {
        Greeting("Android")
    }
}