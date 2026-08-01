package com.yourname.icepacklist.feature.detail.data

import com.squareup.moshi.Moshi
import com.yourname.icepacklist.core.cache.CacheConfig
import com.yourname.icepacklist.core.database.dao.MovieDetailCacheDao
import com.yourname.icepacklist.core.database.dao.PersonCacheDao
import com.yourname.icepacklist.core.database.dao.TvDetailCacheDao
import com.yourname.icepacklist.core.database.entity.MovieDetailCacheEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetailRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val movieDetailCacheDao: MovieDetailCacheDao,
    private val tvDetailCacheDao: TvDetailCacheDao,
    private val personCacheDao: PersonCacheDao,
    private val moshi: Moshi
) {
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetail> {
        val adapter = moshi.adapter(MovieDetail::class.java)
        val cached = movieDetailCacheDao.get(movieId)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: return runCatching { fetchMovieDetails(movieId) })
        }
        return runCatching {
            fetchMovieDetails(movieId).also { detail ->
                movieDetailCacheDao.upsert(
                    MovieDetailCacheEntity(movieId, adapter.toJson(detail), System.currentTimeMillis())
                )
            }
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) } ?: throw error
        }
    }

    private suspend fun fetchMovieDetails(movieId: Int): MovieDetail = withContext(Dispatchers.IO) {
        apiService.getMovieDetails(movieId)
    }
}
