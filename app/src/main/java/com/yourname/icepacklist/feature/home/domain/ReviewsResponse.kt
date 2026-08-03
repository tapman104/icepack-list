package com.yourname.icepacklist.feature.home.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.runtime.Immutable

@Immutable
@JsonClass(generateAdapter = true)
data class Review(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String,
    @Json(name = "content") val content: String,
    @Json(name = "author_details") val authorDetails: AuthorDetails?
)

@JsonClass(generateAdapter = true)
data class AuthorDetails(
    @Json(name = "name") val name: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "avatar_path") val avatarPath: String?,
    @Json(name = "rating") val rating: Double?
)

@JsonClass(generateAdapter = true)
data class ReviewsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<Review>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)
