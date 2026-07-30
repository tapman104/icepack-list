package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Person(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String?,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "birthday") val birthday: String?,
    @Json(name = "known_for_department") val knownForDepartment: String?
)
