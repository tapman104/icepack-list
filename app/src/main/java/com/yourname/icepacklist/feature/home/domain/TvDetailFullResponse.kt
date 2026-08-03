package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class TvDetailFullResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?,
    @Json(name = "genres") val genres: List<Genre> = emptyList(),
    @Json(name = "status") val status: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "origin_country") val originCountry: List<String> = emptyList(),
    @Json(name = "networks") val networksList: List<Network> = emptyList(),
    @Json(name = "created_by") val createdByList: List<CreatedBy> = emptyList(),
    @Json(name = "last_air_date") val lastAirDate: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompany> = emptyList(),
    @Json(name = "spoken_languages") val spokenLanguages: List<SpokenLanguage> = emptyList(),
    @Json(name = "episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    @Json(name = "type") val type: String? = null,
    @Json(name = "credits") val creditsResponse: CreditsResponse? = null,
    @Json(name = "videos") val videoResponse: VideoResponse? = null,
    @Json(name = "similar") val similarResponse: TmdbResponse<TvShow>? = null,
    @Json(name = "watch/providers") val watchProvidersResponse: WatchProvidersResponse? = null,
    @Json(name = "keywords") val keywordsResponse: KeywordsResponse? = null,
    @Json(name = "reviews") val reviewsResponse: ReviewsResponse? = null
) {
    fun toTvShowDetail(): TvShowDetail {
        return TvShowDetail(
            id = id,
            name = name,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            voteAverage = voteAverage,
            firstAirDate = firstAirDate,
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            genres = genres,
            status = status,
            originalLanguage = originalLanguage,
            originCountry = originCountry,
            networksList = networksList,
            createdByList = createdByList,
            lastAirDate = lastAirDate,
            tagline = tagline,
            productionCompanies = productionCompanies,
            spokenLanguages = spokenLanguages,
            episodeRunTime = episodeRunTime,
            type = type,
            creditsResponse = creditsResponse,
            videoResponse = videoResponse
        )
    }
}
