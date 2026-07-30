package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MultiSearchResult(
    @Json(name = "id") val id: Int,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "title") val title: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?
)
