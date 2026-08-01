package com.yourname.icepacklist.core.cache

object CacheConfig {
    const val TTL_24H = 24 * 60 * 60 * 1000L
    const val TTL_6H  =  6 * 60 * 60 * 1000L

    fun isStale(fetchedAt: Long, ttlMs: Long = TTL_24H): Boolean =
        System.currentTimeMillis() - fetchedAt > ttlMs
}
