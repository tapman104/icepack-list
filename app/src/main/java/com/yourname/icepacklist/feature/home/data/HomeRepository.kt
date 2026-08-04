package com.yourname.icepacklist.feature.home.data

import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.datastore.cacheKey
import com.yourname.icepacklist.core.datastore.isAll
import com.yourname.icepacklist.core.datastore.originCountryParam
import com.yourname.icepacklist.core.datastore.withGenresParam
import com.yourname.icepacklist.core.datastore.withoutGenresParam
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
class HomeRepository @Inject constructor(
    private val apiService: TmdbApiService,
    private val cacheDao: HomeListCacheDao,
    private val moshi: Moshi,
    private val watchlistDao: WatchlistDao
) {
    suspend fun getRecommendations(filter: Set<ContentFilter>): Result<List<Any>> {
        val seed = watchlistDao.getLatestWatchlistItem()
        val seedId = seed?.id ?: "popular"
        val key = "recommendations_${filter.cacheKey()}_${seedId}"
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()

        if (seed != null && !filter.isAll()) {
            return runCatching {
                val (movies, tvs) = coroutineScope {
                    val m = async { apiService.discoverMovie(originCountry = originCountry, withGenres = withGenres, withoutGenres = withoutGenres, sortBy = "popularity.desc", page = 1) }
                    val t = async { apiService.discoverTv(originCountry = originCountry, withGenres = withGenres, withoutGenres = withoutGenres, sortBy = "popularity.desc", page = 1) }
                    Pair(m.await(), t.await())
                }
                
                val merged = mutableListOf<Any>()
                val mIter = movies.results.iterator()
                val tIter = tvs.results.iterator()
                while (mIter.hasNext() || tIter.hasNext()) {
                    if (mIter.hasNext()) merged.add(mIter.next())
                    if (tIter.hasNext()) merged.add(tIter.next())
                    if (merged.size >= 20) break
                }
                merged.take(20)
            }
        }

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
                    if (filter.isAll()) {
                        apiService.getPopularMovies(page = 1).results.take(12)
                    } else {
                        apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "popularity.desc").results.take(12)
                    }
                } else {
                    apiService.getMovieRecommendations(seed.id, page = 1).results.take(12)
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
                val result = apiService.getTvRecommendations(seed.id, page = 1).results.take(12)
                cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
                result
            }.recoverCatching { error ->
                cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
            }
        }
    }

    suspend fun getTrendingMovies(filter: Set<ContentFilter>): Result<List<Movie>> {
        val key = "trending_movie_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getTrendingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularMovies(filter: Set<ContentFilter>): Result<List<Movie>> {
        val key = "popular_movie_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getPopularMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getNowPlayingMovies(filter: Set<ContentFilter>): Result<List<Movie>> {
        val key = "now_playing_movie_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getNowPlayingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "primary_release_date.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getUpcomingMovies(filter: Set<ContentFilter>): Result<List<Movie>> {
        val key = "upcoming_movie_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getUpcomingMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "primary_release_date.asc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedMovies(filter: Set<ContentFilter>): Result<List<Movie>> {
        val key = "top_rated_movie_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<Movie>>(
            Types.newParameterizedType(List::class.java, Movie::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getTopRatedMovies(page = 1).results.take(12)
            } else {
                apiService.discoverMovie(originCountry, withGenres, withoutGenres, sortBy = "vote_average.desc", voteCountGte = 200).results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTrendingTvShows(filter: Set<ContentFilter>): Result<List<TvShow>> {
        val key = "trending_tv_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getTrendingTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(originCountry, withGenres, withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getPopularTvShows(filter: Set<ContentFilter>): Result<List<TvShow>> {
        val key = "popular_tv_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getPopularTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(originCountry, withGenres, withoutGenres, sortBy = "popularity.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getTopRatedTvShows(filter: Set<ContentFilter>): Result<List<TvShow>> {
        val key = "top_rated_tv_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getTopRatedTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(originCountry, withGenres, withoutGenres, sortBy = "vote_average.desc", voteCountGte = 200).results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }

    suspend fun getAiringTodayTvShows(filter: Set<ContentFilter>): Result<List<TvShow>> {
        val key = "airing_today_tv_${filter.cacheKey()}"
        val adapter = moshi.adapter<List<TvShow>>(
            Types.newParameterizedType(List::class.java, TvShow::class.java)
        )
        val cached = cacheDao.get(key)
        if (cached != null && !CacheConfig.isStale(cached.fetchedAt)) {
            return Result.success(adapter.fromJson(cached.json) ?: emptyList())
        }
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()
        return runCatching {
            val result = if (filter.isAll()) {
                apiService.getAiringTodayTvShows(page = 1).results.take(12)
            } else {
                apiService.discoverTv(originCountry, withGenres, withoutGenres, sortBy = "first_air_date.desc").results.take(12)
            }
            cacheDao.upsert(HomeListCacheEntity(key, adapter.toJson(result), System.currentTimeMillis()))
            result
        }.recoverCatching { error ->
            cached?.let { adapter.fromJson(it.json) ?: emptyList() } ?: throw error
        }
    }
}
