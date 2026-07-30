package com.yourname.icepacklist.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.ArrayDeque
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimitInterceptor @Inject constructor(
    private val maxRequests: Int = 40,
    private val windowMs: Long = 10_000L
) : Interceptor {

    private val timestamps = ArrayDeque<Long>()
    private val lock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val now = System.currentTimeMillis()
            // Drop timestamps outside the window
            while (timestamps.isNotEmpty() && now - timestamps.peek() > windowMs) {
                timestamps.poll()
            }
            if (timestamps.size >= maxRequests) {
                val oldest = requireNotNull(timestamps.peek())
                val sleepMs = windowMs - (now - oldest) + 50
                if (sleepMs > 0) Thread.sleep(sleepMs)
            }
            timestamps.offer(System.currentTimeMillis())
        }
        return chain.proceed(chain.request())
    }
}
