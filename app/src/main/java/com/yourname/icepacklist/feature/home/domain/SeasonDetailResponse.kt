package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SeasonDetailResponse(
    @Json(name = "_id") val idString: String? = null,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "episodes") val episodes: List<Episode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Episode(
    @Json(name = "id") val id: Int,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "still_path") val stillPath: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null
)
