package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@Deprecated("Use the version with kotlin.time APIs instead.")
fun OkHttpClient.Builder.rateLimit(
    permits: Int,
    period: Long = 1,
    unit: TimeUnit = TimeUnit.SECONDS,
) = addInterceptor(RateLimitInterceptor(null, permits, period.toDuration(unit.toDurationUnit())))

fun OkHttpClient.Builder.rateLimit(permits: Int, period: Duration = 1.seconds) =
    addInterceptor(RateLimitInterceptor(null, permits, period))

internal class RateLimitInterceptor(
    private val host: String?,
    private val permits: Int,
    period: Duration,
) : Interceptor {

    private val requestQueue = ArrayDeque<Long>(permits)
    private val rateLimitMillis = period.inWholeMilliseconds
    private val fairLock = Semaphore(1, true)

    override fun intercept(chain: Interceptor.Chain): Response {
        val call = chain.call()
        if (call.isCanceled()) throw IOException("Canceled")

        val request = chain.request()
        when (host) {
            null, request.url.host -> {}
            else -> return chain.proceed(request)
        }

        try {
            fairLock.acquire()
        } catch (e: InterruptedException) {
            throw IOException(e)
        }

        val queue = requestQueue
        val timestamp: Long
        try {
            synchronized(queue) {
                while (queue.size >= permits) {
                    val periodStart = System.currentTimeMillis() - rateLimitMillis
                    var removedExpired = false
                    while (!queue.isEmpty() && queue.first <= periodStart) {
                        queue.removeFirst()
                        removedExpired = true
                    }
                    if (call.isCanceled()) {
                        throw IOException("Canceled")
                    } else if (removedExpired) {
                        break
                    } else {
                        try {
                            (queue as java.lang.Object).wait(queue.first - periodStart)
                        } catch (_: InterruptedException) {
                            continue
                        }
                    }
                }
                timestamp = System.currentTimeMillis()
                queue.addLast(timestamp)
            }
        } finally {
            fairLock.release()
        }

        val response = chain.proceed(request)
        if (response.networkResponse == null) {
            synchronized(queue) {
                if (queue.isEmpty() || timestamp < queue.first) return@synchronized
                queue.removeFirstOccurrence(timestamp)
                (queue as java.lang.Object).notifyAll()
            }
        }
        return response
    }
}
