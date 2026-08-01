package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersonMovieCredit(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "release_date") val releaseDate: String?
)

@JsonClass(generateAdapter = true)
data class PersonMovieCreditsResponse(
    @Json(name = "cast") val cast: List<PersonMovieCredit> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PersonImage(
    @Json(name = "file_path") val filePath: String
)

@JsonClass(generateAdapter = true)
data class PersonImagesResponse(
    @Json(name = "profiles") val profiles: List<PersonImage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Person(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String?,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "birthday") val birthday: String?,
    @Json(name = "place_of_birth") val placeOfBirth: String?,
    @Json(name = "known_for_department") val knownForDepartment: String?
)
