package com.yourname.icepacklist.feature.home.data

import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: TmdbApiService
) {
    suspend fun getTrendingMovies(): Result<List<Movie>> = runCatching {
        apiService.getTrendingMovies(page = 1).results.take(12)
    }
    suspend fun getPopularMovies(): Result<List<Movie>> = runCatching {
        apiService.getPopularMovies(page = 1).results.take(12)
    }
    suspend fun getNowPlayingMovies(): Result<List<Movie>> = runCatching {
        apiService.getNowPlayingMovies(page = 1).results.take(12)
    }
    suspend fun getUpcomingMovies(): Result<List<Movie>> = runCatching {
        apiService.getUpcomingMovies(page = 1).results.take(12)
    }
    suspend fun getTopRatedMovies(): Result<List<Movie>> = runCatching {
        apiService.getTopRatedMovies(page = 1).results.take(12)
    }
    suspend fun getTrendingTvShows(): Result<List<TvShow>> = runCatching {
        apiService.getTrendingTvShows(page = 1).results.take(12)
    }
    suspend fun getPopularTvShows(): Result<List<TvShow>> = runCatching {
        apiService.getPopularTvShows(page = 1).results.take(12)
    }
    suspend fun getTopRatedTvShows(): Result<List<TvShow>> = runCatching {
        apiService.getTopRatedTvShows(page = 1).results.take(12)
    }
    suspend fun getAiringTodayTvShows(): Result<List<TvShow>> = runCatching {
        apiService.getAiringTodayTvShows(page = 1).results.take(12)
    }
}
