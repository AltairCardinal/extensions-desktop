package eu.kanade.tachiyomi.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

open class NetworkHelper(
    open val client: OkHttpClient = defaultClient(),
    open val cloudflareClient: OkHttpClient = client,
) {
    open fun defaultUserAgentProvider(): String = DEFAULT_USER_AGENT

    companion object {
        const val DEFAULT_USER_AGENT = "MihonDesktopExtensions/0.1"

        fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}
