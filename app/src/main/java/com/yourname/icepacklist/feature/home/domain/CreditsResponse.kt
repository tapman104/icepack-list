package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "cast") val cast: List<Cast>
)
