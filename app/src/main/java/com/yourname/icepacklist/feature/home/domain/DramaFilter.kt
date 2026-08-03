package com.yourname.icepacklist.feature.home.domain

sealed class DramaFilter(
    val label: String,
    val originCountry: String? = null,
    val withGenres: String? = null,
    val withoutGenres: String? = null
) {
    object All : DramaFilter("All")
    object KDrama : DramaFilter("K-Drama", originCountry = "KR")
    object JDrama : DramaFilter("J-Drama", originCountry = "JP", withoutGenres = "16")
    object CDrama : DramaFilter("C-Drama", originCountry = "CN")
    object Anime : DramaFilter("Anime", withGenres = "16")

    companion object {
        val entries: List<DramaFilter> by lazy {
            listOf(All, KDrama, JDrama, CDrama, Anime)
        }
    }
}
