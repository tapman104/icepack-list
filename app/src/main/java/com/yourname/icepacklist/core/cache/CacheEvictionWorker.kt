package com.yourname.icepacklist.core.cache

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourname.icepacklist.core.database.dao.HomeListCacheDao
import com.yourname.icepacklist.core.database.dao.MovieDetailCacheDao
import com.yourname.icepacklist.core.database.dao.PersonCacheDao
import com.yourname.icepacklist.core.database.dao.SearchCacheDao
import com.yourname.icepacklist.core.database.dao.TvDetailCacheDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheEvictionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val homeListCacheDao: HomeListCacheDao,
    private val movieDetailCacheDao: MovieDetailCacheDao,
    private val tvDetailCacheDao: TvDetailCacheDao,
    private val personCacheDao: PersonCacheDao,
    private val searchCacheDao: SearchCacheDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val cutoff24h = System.currentTimeMillis() - CacheConfig.TTL_24H
        val cutoff6h  = System.currentTimeMillis() - CacheConfig.TTL_6H
        homeListCacheDao.deleteOlderThan(cutoff24h)
        movieDetailCacheDao.deleteOlderThan(cutoff24h)
        tvDetailCacheDao.deleteOlderThan(cutoff24h)
        personCacheDao.deleteOlderThan(cutoff24h)
        searchCacheDao.deleteOlderThan(cutoff6h)
        return Result.success()
    }
}
