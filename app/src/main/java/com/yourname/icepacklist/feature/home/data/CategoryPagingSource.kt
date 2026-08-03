package com.yourname.icepacklist.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.datastore.isAll
import com.yourname.icepacklist.core.datastore.originCountryParam
import com.yourname.icepacklist.core.datastore.withGenresParam
import com.yourname.icepacklist.core.datastore.withoutGenresParam
import com.yourname.icepacklist.core.network.TmdbApiService
import retrofit2.HttpException
import java.io.IOException

class CategoryPagingSource(
    private val apiService: TmdbApiService,
    private val category: String,
    private val filter: Set<ContentFilter> = setOf(ContentFilter.ALL)
) : PagingSource<Int, Any>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        val position = params.key ?: 1
        // Compute aggregated params once per load call via shared extensions in ContentFilter.kt
        val isAll = filter.isAll()
        val originCountry = filter.originCountryParam()
        val withGenres = filter.withGenresParam()
        val withoutGenres = filter.withoutGenresParam()

        return try {
            val response = when {
                // When filter is ALL — use standard endpoints unchanged
                isAll -> when (category) {
                    "trending_movies"    -> apiService.getTrendingMovies(page = position)
                    "popular_movies"     -> apiService.getPopularMovies(page = position)
                    "now_playing"        -> apiService.getNowPlayingMovies(page = position)
                    "upcoming"           -> apiService.getUpcomingMovies(page = position)
                    "top_rated_movies"   -> apiService.getTopRatedMovies(page = position)
                    "trending_tv"        -> apiService.getTrendingTvShows(page = position)
                    "popular_tv"         -> apiService.getPopularTvShows(page = position)
                    "top_rated_tv"       -> apiService.getTopRatedTvShows(page = position)
                    "airing_today"       -> apiService.getAiringTodayTvShows(page = position)
                    // "recommendations" has no dedicated endpoint — fall through to discover
                    "recommendations"    -> apiService.getPopularMovies(page = position)
                    else -> throw IllegalArgumentException("Unknown category: $category")
                }
                // When filter is active — use discoverMovie / discoverTv with aggregated params
                else -> when (category) {
                    "trending_movies" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "popularity.desc",
                        page = position
                    )
                    "popular_movies" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "popularity.desc",
                        page = position
                    )
                    "recommendations" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "popularity.desc",
                        page = position
                    )
                    "now_playing" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "primary_release_date.desc",
                        page = position
                    )
                    "upcoming" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "primary_release_date.asc",
                        page = position
                    )
                    "top_rated_movies" -> apiService.discoverMovie(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "vote_average.desc",
                        voteCountGte = 200,
                        page = position
                    )
                    "trending_tv" -> apiService.discoverTv(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "popularity.desc",
                        page = position
                    )
                    "popular_tv" -> apiService.discoverTv(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "popularity.desc",
                        page = position
                    )
                    "top_rated_tv" -> apiService.discoverTv(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "vote_average.desc",
                        voteCountGte = 200,
                        page = position
                    )
                    "airing_today" -> apiService.discoverTv(
                        originCountry = originCountry,
                        withGenres = withGenres,
                        withoutGenres = withoutGenres,
                        sortBy = "first_air_date.desc",
                        page = position
                    )
                    else -> throw IllegalArgumentException("Unknown category: $category")
                }
            }
            val nextKey = if (response.page < response.totalPages) response.page + 1 else null
            LoadResult.Page(
                data = response.results as List<Any>,
                prevKey = if (position == 1) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
