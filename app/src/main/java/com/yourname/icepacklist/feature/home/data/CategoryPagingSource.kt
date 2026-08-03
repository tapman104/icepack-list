package com.yourname.icepacklist.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.TvShow
import retrofit2.HttpException
import java.io.IOException

class CategoryPagingSource(
    private val apiService: TmdbApiService,
    private val category: String,
    private val filter: Set<ContentFilter> = setOf(ContentFilter.ALL)
) : PagingSource<Int, Any>() {

    private val isAll: Boolean
        get() = filter.isEmpty() || filter.contains(ContentFilter.ALL)

    private val originCountry: String?
        get() {
            if (isAll) return null
            val values = filter.mapNotNull { it.originCountry }
            return if (values.isEmpty()) null else values.joinToString("|")
        }

    private val withGenres: String?
        get() {
            if (isAll) return null
            val values = filter.mapNotNull { it.withGenres }
            return if (values.isEmpty()) null else values.joinToString(",")
        }

    private val withoutGenres: String?
        get() {
            if (isAll) return null
            val values = filter.mapNotNull { it.withoutGenres }
            return if (values.isEmpty()) null else values.joinToString(",")
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        val position = params.key ?: 1
        return try {
            val response = when {
                // When filter is ALL — use existing standard endpoints unchanged
                isAll -> when (category) {
                    "trending_movies" -> apiService.getTrendingMovies(page = position)
                    "popular_movies"  -> apiService.getPopularMovies(page = position)
                    "now_playing"     -> apiService.getNowPlayingMovies(page = position)
                    "upcoming"        -> apiService.getUpcomingMovies(page = position)
                    "top_rated_movies"-> apiService.getTopRatedMovies(page = position)
                    "trending_tv"     -> apiService.getTrendingTvShows(page = position)
                    "popular_tv"      -> apiService.getPopularTvShows(page = position)
                    "top_rated_tv"    -> apiService.getTopRatedTvShows(page = position)
                    "airing_today"    -> apiService.getAiringTodayTvShows(page = position)
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
