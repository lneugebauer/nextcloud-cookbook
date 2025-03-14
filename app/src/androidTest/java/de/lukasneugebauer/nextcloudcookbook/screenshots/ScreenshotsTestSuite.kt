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
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.IconVisibility
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType

@RunWith(AndroidJUnit4::class)
class ScreenshotsTestSuite {
    private val uiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
