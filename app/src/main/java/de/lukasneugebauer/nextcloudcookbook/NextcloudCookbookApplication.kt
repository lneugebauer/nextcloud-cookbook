package de.lukasneugebauer.nextcloudcookbook

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import de.lukasneugebauer.nextcloudcookbook.core.util.OkHttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.acra.ACRA
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidApp
class NextcloudCookbookApplication : Application() {
    @Inject
    lateinit var clientProvider: OkHttpClientProvider

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeImageLoader()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        initAcra()
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeImageLoader() {
        val initialImageLoader = createImageLoader(clientProvider.getCurrentClient())
        SingletonImageLoader.setSafe { initialImageLoader }

        // Watch for client changes and update the ImageLoader
        applicationScope.launch {
            clientProvider.clientFlow.collect { client ->
                val newImageLoader = createImageLoader(client)
                SingletonImageLoader.setSafe { newImageLoader }
            }
        }
    }

    private fun createImageLoader(client: OkHttpClient): ImageLoader =
        ImageLoader
            .Builder(this as PlatformContext)
            .components {
                add(OkHttpNetworkFetcherFactory(client))
            }.apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }.build()

    private fun initAcra() {
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

            ACRA.errorReporter.putCustomData("productFlavor", BuildConfig.FLAVOR)
        }
    }
}
