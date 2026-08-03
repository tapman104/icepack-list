package com.yourname.icepacklist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.size.Precision
import com.yourname.icepacklist.core.cache.CacheEvictionWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class IcepackApplication : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .respectCacheHeaders(false)
            .precision(Precision.INEXACT)
            .bitmapFactoryMaxParallelism(2)
            .crossfade(false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<CacheEvictionWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cache_eviction",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
