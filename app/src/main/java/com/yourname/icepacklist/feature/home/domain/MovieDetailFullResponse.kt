package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class MovieDetailFullResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "runtime") val runtime: Int?,
    @Json(name = "genres") val genres: List<Genre> = emptyList(),
    @Json(name = "status") val status: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "origin_country") val originCountry: List<String> = emptyList(),
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "budget") val budget: Long? = null,
    @Json(name = "revenue") val revenue: Long? = null,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompany> = emptyList(),
    @Json(name = "spoken_languages") val spokenLanguages: List<SpokenLanguage> = emptyList(),
    @Json(name = "credits") val creditsResponse: CreditsResponse? = null,
    @Json(name = "videos") val videoResponse: VideoResponse? = null,
    @Json(name = "similar") val similarResponse: TmdbResponse<Movie>? = null,
    @Json(name = "watch/providers") val watchProvidersResponse: WatchProvidersResponse? = null,
    @Json(name = "keywords") val keywordsResponse: KeywordsResponse? = null,
    @Json(name = "reviews") val reviewsResponse: ReviewsResponse? = null
) {
    fun toMovieDetail(): MovieDetail {
        return MovieDetail(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            runtime = runtime,
            genres = genres,
            status = status,
            originalLanguage = originalLanguage,
            originCountry = originCountry,
            tagline = tagline,
            budget = budget,
            revenue = revenue,
            productionCompanies = productionCompanies,
            spokenLanguages = spokenLanguages,
            creditsResponse = creditsResponse,
            videoResponse = videoResponse
        )
    }
}
