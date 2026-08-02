package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WatchProvidersResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "results") val results: Map<String, WatchProviderRegion>?
)

@JsonClass(generateAdapter = true)
data class WatchProviderRegion(
    @Json(name = "link") val link: String?,
    @Json(name = "flatrate") val flatrate: List<WatchProvider>?
)

@JsonClass(generateAdapter = true)
data class WatchProvider(
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "logo_path") val logoPath: String?
)
