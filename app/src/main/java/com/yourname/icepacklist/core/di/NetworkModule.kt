package com.yourname.icepacklist.core.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.yourname.icepacklist.core.network.AuthInterceptor
import com.yourname.icepacklist.core.network.RateLimitInterceptor
import com.yourname.icepacklist.core.network.RetryInterceptor
import com.yourname.icepacklist.core.network.TmdbApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            // Rate limiting: max 40 requests per 10 seconds (TMDB limit is 50, stay under)
            .addInterceptor(RateLimitInterceptor(maxRequests = 40, windowMs = 10_000L))
            // Retry on 429 with Retry-After header respect
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            .addInterceptor(loggingInterceptor)
            // Cache: 10 MB HTTP cache for image metadata (not images themselves)
            .cache(Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbApiService(retrofit: Retrofit): TmdbApiService {
        return retrofit.create(TmdbApiService::class.java)
    }
}

