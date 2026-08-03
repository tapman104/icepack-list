package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class Keyword(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class KeywordsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "keywords") val keywords: List<Keyword>? = null,
    @Json(name = "results") val results: List<Keyword>? = null
)
