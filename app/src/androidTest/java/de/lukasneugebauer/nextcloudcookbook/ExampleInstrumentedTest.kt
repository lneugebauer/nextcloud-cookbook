package de.lukasneugebauer.nextcloudcookbook

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.System.console

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull("App context should not be null", appContext)

        val pkg = appContext.packageName
        val expectedPrefix = "de.lukasneugebauer.nextcloudcookbook"
        assertTrue("Package name should start with $expectedPrefix; actual: $pkg", pkg.startsWith(expectedPrefix))

        val pm = appContext.packageManager
        try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            assertNotNull("ApplicationInfo should be retrievable for $pkg", appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            throw AssertionError("Package not found: $pkg", e)
        }
    }
}