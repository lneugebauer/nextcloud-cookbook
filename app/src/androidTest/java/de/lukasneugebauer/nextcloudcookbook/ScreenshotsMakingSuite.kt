package de.lukasneugebauer.nextcloudcookbook

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.login.LoginScreen
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.home.HomeScreen
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.BluetoothState
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.IconVisibility
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType

@RunWith(AndroidJUnit4::class)
class ScreenshotsMakingSuite {

    private val uiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        CleanStatusBar()
            .setBluetoothState(BluetoothState.DISCONNECTED)
            .setMobileNetworkDataType(MobileDataType.LTE)
            .setWifiVisibility(IconVisibility.HIDE)
            .setShowNotifications(false)
            .setClock("0900")
            .setBatteryLevel(100)
            .enable()
    }

    @After
    fun tearDown() {
        CleanStatusBar.disable()
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
                onManualLoginClick = { _, _, _ -> }
            )
        }
    }

    private fun makeScreenshotOf(
        screenshotName: String,
        content: @Composable () -> Unit
    ) {
        composeTestRule.activityRule.scenario.onActivity(
            ComponentActivity::enableEdgeToEdge
        )

        composeTestRule.setContent {
            NextcloudCookbookTheme(content = content)
        }

        uiDevice.waitForIdle()

        Screengrab.screenshot(screenshotName)
    }
}