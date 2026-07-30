package com.yourname.icepacklist.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor(
    private val maxRetries: Int = 3
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response? = null

        while (attempt <= maxRetries) {
            try {
                response?.close()
                response = chain.proceed(chain.request())

                if (response.code == 429) {
                    val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 1L
                    Thread.sleep(retryAfter * 1000)
                    attempt++
                    continue
                }

                return response
            } catch (e: Exception) {
                if (attempt == maxRetries) throw e
                attempt++
            }
        }

        return response!!
    }
}
