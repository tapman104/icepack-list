package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class CreditsResponse(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "cast") val cast: List<Cast> = emptyList(),
    @Json(name = "crew") val crew: List<Crew> = emptyList()
)

