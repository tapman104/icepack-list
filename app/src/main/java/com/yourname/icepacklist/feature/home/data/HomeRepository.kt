package com.yourname.icepacklist.feature.home.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yourname.icepacklist.core.cache.CacheConfig
import com.yourname.icepacklist.core.database.dao.HomeListCacheDao
import com.yourname.icepacklist.core.database.entity.HomeListCacheEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val cacheDao: HomeListCacheDao,
    private val moshi: Moshi
) {
    suspend fun getTrendingMovies(): Result<List<Movie>> {
        val key = "trending_movie"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getTrendingMovies(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularMovies(): Result<List<Movie>> {
        val key = "popular_movie"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getPopularMovies(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getNowPlayingMovies(): Result<List<Movie>> {
        val key = "now_playing_movie"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getNowPlayingMovies(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getUpcomingMovies(): Result<List<Movie>> {
        val key = "upcoming_movie"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getUpcomingMovies(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedMovies(): Result<List<Movie>> {
        val key = "top_rated_movie"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getTopRatedMovies(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTrendingTvShows(): Result<List<TvShow>> {
        val key = "trending_tv"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getTrendingTvShows(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularTvShows(): Result<List<TvShow>> {
        val key = "popular_tv"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getPopularTvShows(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedTvShows(): Result<List<TvShow>> {
        val key = "top_rated_tv"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getTopRatedTvShows(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getAiringTodayTvShows(): Result<List<TvShow>> {
        val key = "airing_today_tv"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = apiService.getAiringTodayTvShows(page = 1).results.take(12)
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }
    suspend fun getDiscoverTv(originCountry: String?, withGenres: String?, withoutGenres: String?): Result<List<TvShow>> {
        return runCatching {
            apiService.discoverTv(originCountry, withGenres, withoutGenres).results.take(12)
        }
    }
}
