package com.yourname.icepacklist.feature.home.domain

import androidx.compose.runtime.Immutable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Immutable
@JsonClass(generateAdapter = true)
data class TvShow(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
)
