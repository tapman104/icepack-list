package com.yourname.icepacklist.core.datastore

enum class ContentFilter(
    val displayName: String,
    val originCountry: String? = null,
    val withGenres: String? = null,
    val withoutGenres: String? = null
) {
    ALL("All Regions"),
    K_DRAMA("K-Drama", originCountry = "KR"),
    J_DRAMA("J-Drama", originCountry = "JP", withoutGenres = "16"),
    C_DRAMA("C-Drama", originCountry = "CN"),
    ANIME("Anime", withGenres = "16"),
    THAI_DRAMA("Thai Drama", originCountry = "TH"),
    INDIAN("Indian", originCountry = "IN"),
    US("US", originCountry = "US"),
    UK("UK", originCountry = "GB");

    companion object {
        fun fromKey(key: String): ContentFilter {
            return entries.find { it.name == key } ?: ALL
        }
    }
}
