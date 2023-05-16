package de.lukasneugebauer.nextcloudcookbook.auth.data.remote

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.R
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// https://gist.github.com/twaddington/e66e495e14950b4437216ab5c704835b
@Singleton
class UserAgentInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val userAgent = String.format(
            Locale.US,
            "%s/%s (Android %s; %s; %s %s; %s)",
            context.getString(R.string.app_name),
            BuildConfig.VERSION_NAME,
            Build.VERSION.RELEASE,
            Build.MODEL,
            Build.BRAND,
            Build.DEVICE,
            Locale.getDefault().language,
        )

        val request = chain.request()
            .newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }
}
