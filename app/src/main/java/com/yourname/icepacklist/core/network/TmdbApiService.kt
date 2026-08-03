package com.yourname.icepacklist.core.network

import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.GenreListResponse
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.feature.home.domain.KeywordsResponse
import com.yourname.icepacklist.feature.home.domain.WatchProvidersResponse
import com.yourname.icepacklist.feature.home.domain.ReviewsResponse
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import com.yourname.icepacklist.feature.home.domain.PersonDetail
import com.yourname.icepacklist.feature.home.domain.TmdbResponse
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.feature.home.domain.VideoResponse
import com.yourname.icepacklist.feature.home.domain.CombinedCreditsResponse
import com.yourname.icepacklist.feature.home.domain.PersonImagesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // ── Movies ──────────────────────────────────────────────────────────────

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): MovieDetail

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): CreditsResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int
    ): VideoResponse

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Int
    ): WatchProvidersResponse

    @GET("movie/{movie_id}/keywords")
    suspend fun getMovieKeywords(
        @Path("movie_id") movieId: Int
    ): KeywordsResponse

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int
    ): ReviewsResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<Movie>

    // ── TV Shows ─────────────────────────────────────────────────────────────

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("tv/airing_today")
    suspend fun getAiringTodayTvShows(
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("trending/tv/{time_window}")
    suspend fun getTrendingTvShows(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): TvShowDetail

    @GET("tv/{tv_id}/credits")
    suspend fun getTvShowCredits(
        @Path("tv_id") tvId: Int
    ): CreditsResponse

    @GET("tv/{tv_id}/similar")
    suspend fun getSimilarTvShows(
        @Path("tv_id") tvId: Int,
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("tv/{tv_id}/videos")
    suspend fun getTvShowVideos(
        @Path("tv_id") tvId: Int
    ): VideoResponse

    @GET("tv/{tv_id}/watch/providers")
    suspend fun getTvWatchProviders(
        @Path("tv_id") tvId: Int
    ): WatchProvidersResponse

    @GET("tv/{tv_id}/keywords")
    suspend fun getTvKeywords(
        @Path("tv_id") tvId: Int
    ): KeywordsResponse

    @GET("tv/{tv_id}/reviews")
    suspend fun getTvReviews(
        @Path("tv_id") tvId: Int
    ): ReviewsResponse

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int
    ): com.yourname.icepacklist.feature.home.domain.SeasonDetailResponse

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("with_origin_country") originCountry: String? = null,
        @Query("with_genres") withGenres: String? = null,
        @Query("without_genres") withoutGenres: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbResponse<TvShow>

    // ── People ───────────────────────────────────────────────────────────────

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int
    ): PersonDetail

    @GET("person/{person_id}/combined_credits")
    suspend fun getPersonCombinedCredits(
        @Path("person_id") personId: Int
    ): CombinedCreditsResponse

    @GET("person/{person_id}/images")
    suspend fun getPersonImages(
        @Path("person_id") personId: Int
    ): PersonImagesResponse

    // ── Genres ───────────────────────────────────────────────────────────────

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(): GenreListResponse

    // ── Multi-search ─────────────────────────────────────────────────────────

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse<MultiSearchResult>
}
