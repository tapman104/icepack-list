package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Network(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class CreatedBy(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class TvShowDetail(
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
    @Json(name = "credits") val creditsResponse: CreditsResponse? = null,
    @Json(name = "videos") val videoResponse: VideoResponse? = null,
    @Json(ignore = true) val networks: List<String> = emptyList(),
    @Json(ignore = true) val createdBy: String = "",
    @Json(ignore = true) val videos: List<VideoResult> = emptyList(),
    @Json(ignore = true) val similar: List<TvShow> = emptyList()
)
