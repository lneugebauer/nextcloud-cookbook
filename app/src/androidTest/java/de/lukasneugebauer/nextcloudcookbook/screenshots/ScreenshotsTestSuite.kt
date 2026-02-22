package de.lukasneugebauer.nextcloudcookbook.screenshots

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.manual.ManualLoginLayout
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.manual.TopAppBar
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.start.StartLayout
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AppState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBarContent
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBarDestination
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Tool
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.HomeScreenState
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail.RecipeDetailLayout
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.home.HomeScreen
import org.junit.AfterClass
import org.junit.Assume
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.IconVisibility
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class ScreenshotsTestSuite {
    private val uiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    init {
        val iSFastlane = InstrumentationRegistry.getArguments().getString("fastlane-screenshots")
        Assume.assumeTrue(
            "This test suite should run only with 'fastlane screenshots'",
            "true" == iSFastlane,
        )
    }

    @Test
    fun loginScreen() {
        makeScreenshotOf("1") {
            StartLayout(
                url = "",
                allowSelfSignedCertificates = false,
                onUrlChange = {},
                onAllowSelfSignedCertificatesChange = {},
                onWebViewLoginClick = {},
                onManualLoginClick = {},
            )
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Test
    fun manualLoginScreen() {
        makeScreenshotOf("2") {
            Scaffold(
                topBar = { TopAppBar(onBackClick = {}) },
            ) { _ ->
                ManualLoginLayout(
                    url = "https://cloud.example.com",
                    username = "",
                    password = "",
                    onUsernameChange = {},
                    onPasswordChange = {},
                    onLoginClick = {},
                )
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Test
    fun homeScreen() {
        makeScreenshotOf("3") {
            val data =
                listOf(
                    HomeScreenDataResult.Single(
                        headline = R.string.home_recommendation,
                        recipe = RECIPE,
                    ),
                    HomeScreenDataResult.Row(
                        headline = "Dinner",
                        recipes =
                            listOf(
                                RECIPE_PREVIEW.copy(name = "Pizza", imageUrl = "aurelien_lemasson_theobald_x00czbt4dfk_unsplash"),
                                RECIPE_PREVIEW.copy(name = "Pasta", imageUrl = "eaters_collective_12ehc6fxpyg_unsplash"),
                                RECIPE_PREVIEW.copy(name = "Fried chicken", imageUrl = "marcin_andrzejewski_ltlniuw9xwe_unsplash"),
                            ),
                    ),
                    HomeScreenDataResult.Row(
                        headline = "Breakfast",
                        recipes =
                            listOf(
                                RECIPE_PREVIEW.copy(name = "Sandwich", imageUrl = "ella_olsson_2ixtgsgfi_s_unsplash"),
                            ),
                    ),
                )
            val uiState = HomeScreenState.Loaded(data = data)
            Scaffold(
                bottomBar = {
                    BottomBarContent(selected = BottomBarDestination.Home, onClick = {})
                },
            ) { _ ->
                HomeScreen(
                    uiState = uiState,
                    onSettingsIconClick = {},
                    onHeadlineClick = {},
                    onRecipeClick = {},
                )
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Test
    fun detailScreen() {
        makeScreenshotOf("4") {
            Scaffold(
                bottomBar = {
                    BottomBarContent(selected = BottomBarDestination.Recipes, onClick = {})
                },
            ) { _ ->
                RecipeDetailLayout(
                    recipe = RECIPE,
                    calculatedIngredients = emptyList(),
                    currentYield = RECIPE.yield,
                    onDecreaseYield = {},
                    onIncreaseYield = {},
                    onNavIconClick = {},
                    onDetailImageClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    shareText = "",
                    onFabClick = {},
                    loading = false,
                    errorMessage = null,
                    error = null,
                    onKeywordClick = {},
                    onResetYield = {},
                    isShowIngredientSyntaxIndicator = true,
                )
            }
        }
    }

    private fun makeScreenshotOf(
        screenshotName: String,
        content: @Composable () -> Unit,
    ) {
        composeTestRule.activityRule.scenario.onActivity(
            ComponentActivity::enableEdgeToEdge,
        )

        composeTestRule.setContent {
            NextcloudCookbookTheme {
                CompositionLocalProvider(LocalInspectionMode provides true, LocalAppState provides AppState()) {
                    content()
                }
            }
        }

        uiDevice.waitForIdle()

        Screengrab.screenshot(screenshotName)
    }

    companion object {
        val RECIPE =
            Recipe(
                id = "1",
                name = "Pizza",
                description = "The most delicious pizza in the world",
                url = "https://www.example.com",
                imageOrigin = "https://www.example.com/image.jpg",
                imageUrl = "aurelien_lemasson_theobald_x00czbt4dfk_unsplash",
                category = "Dinner",
                keywords = listOf("Pizza", "Italian", "Homemade"),
                yield = 4,
                prepTime = Duration.parse("PT1H00M0S"),
                cookTime = Duration.parse("PT0H10M0S"),
                totalTime = Duration.parse("PT2H10M0S"),
                nutrition = null,
                tools =
                    List(1) {
                        Tool(id = it, value = "Lorem ipsum")
                    },
                ingredients =
                    listOf(
                        Ingredient(id = 1, value = "## Dough"),
                        Ingredient(id = 2, value = "1000 g flour"),
                        Ingredient(id = 3, value = "650 ml room temperature water"),
                        Ingredient(id = 4, value = "20 g salt"),
                        Ingredient(id = 5, value = "3 g active dry yeast"),
                        Ingredient(id = 5, value = "## Sauce"),
                        Ingredient(id = 5, value = "1 can tomatoes"),
                        Ingredient(id = 5, value = "2 gloves garlic"),
                        Ingredient(id = 5, value = "2 tbsp olive oil"),
                        Ingredient(id = 5, value = "3 g salt"),
                        Ingredient(id = 5, value = "fresh basil"),
                    ),
                instructions =
                    List(1) {
                        Instruction(
                            id = it,
                            value =
                                """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                        |eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                        |voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
                        |clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
                        |amet.
                                """.trimMargin(),
                        )
                    },
                createdAt = "",
                modifiedAt = "",
            )

        val RECIPE_PREVIEW =
            RecipePreview(
                id = "1",
                name = "Lorem ipsum",
                keywords = emptySet<String>(),
                category = "Lorem ipsum",
                imageUrl = "",
                createdAt = "",
                modifiedAt = "",
            )

        @JvmStatic
        @BeforeClass
        fun beforeAll() {
            CleanStatusBar()
                .setMobileNetworkDataType(MobileDataType.LTE)
                .setMobileNetworkLevel(4)
                .setWifiVisibility(IconVisibility.HIDE)
                .setClock("0900")
                .enable()
        }

        @JvmStatic
        @AfterClass
        fun afterAll() {
            CleanStatusBar.disable()
        }
    }
}
