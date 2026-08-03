package com.yourname.icepacklist.feature.home.data

import com.yourname.icepacklist.core.datastore.ContentFilter

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yourname.icepacklist.core.cache.CacheConfig
import com.yourname.icepacklist.core.database.dao.HomeListCacheDao
import com.yourname.icepacklist.core.database.entity.HomeListCacheEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.MediaType
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val cacheDao: HomeListCacheDao,
    private val moshi: Moshi,
    private val watchlistDao: WatchlistDao
) {
    suspend fun getRecommendations(filter: ContentFilter): Result<List<Any>> {
        val seed = watchlistDao.getLatestWatchlistItem()
        val seedId = seed?.id ?: "popular"
        val key = "recommendations_${filter.name}_${seedId}"
        
        if (seed == null || seed.mediaType == MediaType.MOVIE) {
            val adapter = moshi.adapter<List<Movie>>(
                Types.newParameterizedType(List::class.java, Movie::class.java)
            )
            val cached = cacheDao.get(key)
            if (cached != null && !com.yourname.icepacklist.core.cache.CacheConfig.isStale(cached.fetchedAt)) {
                return Result.success(adapter.fromJson(cached.json) ?: emptyList())
            }
            return runCatching {
                val result = if (seed == null) {
                    if (filter == ContentFilter.ALL) {
                        apiService.getPopularMovies(page = 1).results.take(12)
                    } else {
                        apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "popularity.desc").results.take(12)
                    }
                } else {
                    var rawResults = apiService.getMovieRecommendations(seed.id, page = 1).results
                    if (filter != ContentFilter.ALL && filter.originCountry != null) {
                        rawResults = rawResults.filter { it.originCountry?.contains(filter.originCountry) == true }
                    }
                    rawResults.take(12)
                }
                cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
                result
            }.recoverCatching { error ->
                cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
            }
        } else {
            val adapter = moshi.adapter<List<TvShow>>(
                Types.newParameterizedType(List::class.java, TvShow::class.java)
            )
            val cached = cacheDao.get(key)
            if (cached != null && !com.yourname.icepacklist.core.cache.CacheConfig.isStale(cached.fetchedAt)) {
                return Result.success(adapter.fromJson(cached.json) ?: emptyList())
            }
            return runCatching {
                var rawResults = apiService.getTvRecommendations(seed.id, page = 1).results
                if (filter != ContentFilter.ALL && filter.originCountry != null) {
                    rawResults = rawResults.filter { it.originCountry?.contains(filter.originCountry) == true }
                }
                val result = rawResults.take(12)
                cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
                result
            }.recoverCatching { error ->
                cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
            }
        }
    }

    suspend fun getTrendingMovies(filter: ContentFilter): Result<List<Movie>> {
        val key = "trending_movie_${filter.name}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getTrendingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularMovies(filter: ContentFilter): Result<List<Movie>> {
        val key = "popular_movie_${filter.name}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getPopularMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getNowPlayingMovies(filter: ContentFilter): Result<List<Movie>> {
        val key = "now_playing_movie_${filter.name}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getNowPlayingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "primary_release_date.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getUpcomingMovies(filter: ContentFilter): Result<List<Movie>> {
        val key = "upcoming_movie_${filter.name}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getUpcomingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "primary_release_date.asc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedMovies(filter: ContentFilter): Result<List<Movie>> {
        val key = "top_rated_movie_${filter.name}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getTopRatedMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "vote_average.desc", voteCountGte = 200).results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTrendingTvShows(filter: ContentFilter): Result<List<TvShow>> {
        val key = "trending_tv_${filter.name}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getTrendingTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularTvShows(filter: ContentFilter): Result<List<TvShow>> {
        val key = "popular_tv_${filter.name}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getPopularTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedTvShows(filter: ContentFilter): Result<List<TvShow>> {
        val key = "top_rated_tv_${filter.name}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getTopRatedTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "vote_average.desc", voteCountGte = 200).results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getAiringTodayTvShows(filter: ContentFilter): Result<List<TvShow>> {
        val key = "airing_today_tv_${filter.name}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        return runCatching {
            val result = if (filter == ContentFilter.ALL) {
                apiService.getAiringTodayTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(filter.originCountry, filter.withGenres, filter.withoutGenres, sortBy = "first_air_date.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }
}
