package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class CombinedCreditsCast(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "character") val character: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "episode_count") val episodeCount: Int?,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val displayYear: String get() = (releaseDate ?: firstAirDate)?.take(4) ?: ""
    val displayCharacter: String get() = character ?: ""
}

@Immutable
@JsonClass(generateAdapter = true)
data class CombinedCreditsResponse(
    @Json(name = "cast") val cast: List<CombinedCreditsCast> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PersonImage(
    @Json(name = "file_path") val filePath: String
)

@JsonClass(generateAdapter = true)
data class PersonImagesResponse(
    @Json(name = "profiles") val profiles: List<PersonImage> = emptyList()
)

@Immutable
@JsonClass(generateAdapter = true)
data class PersonDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String?,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "birthday") val birthday: String?,
    @Json(name = "place_of_birth") val placeOfBirth: String?,
    @Json(name = "known_for_department") val knownForDepartment: String?,
    @Json(name = "deathday") val deathday: String?,
    @Json(name = "gender") val gender: Int?
)
