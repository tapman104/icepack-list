package com.yourname.icepacklist.feature.home.domain

import androidx.compose.runtime.Immutable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Immutable
@JsonClass(generateAdapter = true)
data class MultiSearchResult(
    @Json(name = "id") val id: Int,
    @Json(name = "media_type") val mediaType: String,   // "movie", "tv", "person"
    @Json(name = "title") val title: String?,            // movies
    @Json(name = "name") val name: String?,              // tv + person
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "profile_path") val profilePath: String?,  // person
    @Json(name = "overview") val overview: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "origin_country") val originCountry: List<String>?,
    @Json(name = "genre_ids") val genreIds: List<Int>?
) {
    val displayTitle: String get() = title ?: name ?: ""
    val displayDate: String get() = releaseDate ?: firstAirDate ?: ""
    val displayPoster: String get() = posterPath ?: profilePath ?: ""

    companion object {
        fun fromMovie(movie: Movie): MultiSearchResult {
            return MultiSearchResult(
                id = movie.id,
                mediaType = "movie",
                title = movie.title,
                name = null,
                posterPath = movie.posterPath,
                profilePath = null,
                overview = movie.overview,
                voteAverage = movie.voteAverage,
                releaseDate = movie.releaseDate,
                firstAirDate = null,
                originCountry = null,
                genreIds = movie.genreIds
            )
        }

        fun fromTv(tv: TvShow): MultiSearchResult {
            return MultiSearchResult(
                id = tv.id,
                mediaType = "tv",
                title = null,
                name = tv.name,
                posterPath = tv.posterPath,
                profilePath = null,
                overview = tv.overview,
                voteAverage = tv.voteAverage,
                releaseDate = null,
                firstAirDate = tv.firstAirDate,
                originCountry = tv.originCountry,
                genreIds = tv.genreIds
            )
        }
    }
}
