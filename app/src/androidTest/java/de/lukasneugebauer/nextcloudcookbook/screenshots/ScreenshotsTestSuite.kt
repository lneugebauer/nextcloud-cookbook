package de.lukasneugebauer.nextcloudcookbook.screenshots

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.login.LoginScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Tool
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail.RecipeDetailContent
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
        makeScreenshotOf("login") {
            LoginScreen(
                showManualLogin = false,
                usernameError = null,
                passwordError = null,
                urlError = null,
                onClearError = {},
                onLoginClick = {},
                onShowManualLoginClick = {},
                onManualLoginClick = { _, _, _ -> },
            )
        }
    }

    @Test
    fun manualLoginScreen() {
        makeScreenshotOf("manual-login") {
            LoginScreen(
                showManualLogin = true,
                usernameError = null,
                passwordError = null,
                urlError = null,
                onClearError = {},
                onLoginClick = {},
                onShowManualLoginClick = {},
                onManualLoginClick = { _, _, _ -> },
            )
        }
    }

    @Test
    fun homeScreen() {
        makeScreenshotOf("home") {
            TODO("Create home screen content composable that can easily be filled with preview data")
        }
    }

    @Test
    fun detailScreen() {
        makeScreenshotOf("detail") {
            TODO("Overwrite AuthorizedImage composable to be able to show a static image from sample data directory")
            RecipeDetailContent(
                recipe = RECIPE,
                calculatedIngredients = emptyList(),
                currentYield = 2,
                onDecreaseYield = {},
                onIncreaseYield = {},
                onKeywordClick = {},
                onResetYield = {},
            )
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
            NextcloudCookbookTheme(content = content)
        }

        uiDevice.waitForIdle()

        Screengrab.screenshot(screenshotName)
    }

    companion object {
        val RECIPE =
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
                        Tool(id = it, value = "Lorem ipsum")
                    },
                ingredients =
                    List(2) {
                        Ingredient(id = it, value = "Lorem ipsum")
                    },
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
