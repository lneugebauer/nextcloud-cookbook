package de.lukasneugebauer.nextcloudcookbook

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import timber.log.Timber
import java.time.LocalDate

@HiltAndroidApp
class NextcloudCookbookApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeTimber()
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        if (!BuildConfig.DEBUG) {
            initAcra {
                buildConfigClass = BuildConfig::class.java
                reportFormat = StringFormat.JSON

                dialog {
                    title = getString(R.string.app_name)
                    text = getString(R.string.dialog_crash_report_text)
                    negativeButtonText = getString(R.string.common_no)
                    positiveButtonText = getString(R.string.common_yes)
                    resTheme = android.R.style.Theme_DeviceDefault_Dialog
                }

                mailSender {
                    mailTo = "kontakt+nextcloudcookbook@lukasneugebauer.de"
                    reportFileName =
                        "ACRA-report_${LocalDate.now()}_v${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}.txt"
                    subject = "Nextcloud Cookbook crash report"
                }
            }
        }
    }
}
