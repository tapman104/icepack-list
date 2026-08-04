package com.yourname.icepacklist.core.network

import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val apiKeyDataStore: ApiKeyDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Read the API key and adult content enabled synchronously
        val apiKey = runBlocking { apiKeyDataStore.apiKey.first() }
        val adultContentEnabled = runBlocking { apiKeyDataStore.adultContentEnabled.first() }

        val url = request.url.newBuilder()
        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException("No API key configured")
        }
        url.addQueryParameter("api_key", apiKey)
        url.addQueryParameter("include_adult", adultContentEnabled.toString())
        
        if (!adultContentEnabled && request.url.encodedPath.contains("/discover/")) {
            url.addQueryParameter("without_genres", "10749")
        }
        
        val newRequest = request.newBuilder()
            .url(url.build())
            .build()
            
        return chain.proceed(newRequest)
    }
}
