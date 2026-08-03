package com.yourname.icepacklist.feature.detail.data

import com.squareup.moshi.Moshi
import com.yourname.icepacklist.core.cache.CacheConfig
import com.yourname.icepacklist.core.database.dao.MovieDetailCacheDao
import com.yourname.icepacklist.core.database.dao.PersonCacheDao
import com.yourname.icepacklist.core.database.dao.TvDetailCacheDao
import com.yourname.icepacklist.core.database.entity.MovieDetailCacheEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.MovieDetailFullResponse
import com.yourname.icepacklist.feature.home.domain.PersonDetail
import com.yourname.icepacklist.feature.home.domain.TvDetailFullResponse
import com.yourname.icepacklist.feature.home.domain.CombinedCreditsResponse
import com.yourname.icepacklist.feature.home.domain.PersonImagesResponse
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

    suspend fun getMovieDetailFull(movieId: Int): Result<MovieDetailFullResponse> {
        val adapter = moshi.adapter(MovieDetailFullResponse::class.java)
        val cached = movieDetailCacheDao.get(movieId)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            val fromCache = runCatching { adapter.fromJson(cached.json) }.getOrNull()
            if (fromCache != null) return Result.success(fromCache)
        }
        return runCatching {
            withContext(Dispatchers.IO) {
                apiService.getMovieDetailFull(movieId).also { detail ->
                    movieDetailCacheDao.upsert(
                        MovieDetailCacheEntity(movieId, adapter.toJson(detail), System.currentTimeMillis())
                    )
                }
            }
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) } ?: throw error
        }
    }

    suspend fun getTvDetailFull(tvId: Int): Result<TvDetailFullResponse> {
        val adapter = moshi.adapter(TvDetailFullResponse::class.java)
        val cached = tvDetailCacheDao.get(tvId)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            val fromCache = runCatching { adapter.fromJson(cached.json) }.getOrNull()
            if (fromCache != null) return Result.success(fromCache)
        }
        return runCatching {
            withContext(Dispatchers.IO) {
                apiService.getTvDetailFull(tvId).also { detail ->
                    tvDetailCacheDao.upsert(
                        com.yourname.icepacklist.core.database.entity.TvDetailCacheEntity(
                            tvId, adapter.toJson(detail), System.currentTimeMillis()
                        )
                    )
                }
            }
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) } ?: throw error
        }
    }

    suspend fun getPersonDetail(personId: Int): PersonDetail = withContext(Dispatchers.IO) {
        apiService.getPersonDetails(personId)
    }

    suspend fun getPersonCombinedCredits(personId: Int): CombinedCreditsResponse = withContext(Dispatchers.IO) {
        apiService.getPersonCombinedCredits(personId)
    }

    suspend fun getPersonImages(personId: Int): PersonImagesResponse = withContext(Dispatchers.IO) {
        apiService.getPersonImages(personId)
    }
}
