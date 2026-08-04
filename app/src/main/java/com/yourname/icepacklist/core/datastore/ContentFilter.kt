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

        fun toKey(filters: Set<ContentFilter>): String {
            if (filters.isEmpty() || filters.contains(ALL)) return "ALL"
            return filters.joinToString(",") { it.name }
        }

        fun fromKeys(key: String): Set<ContentFilter> {
            if (key.isBlank()) return setOf(ALL)
            val result = key.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { fromKey(it) }
                .toSet()
            return if (result.isEmpty()) setOf(ALL) else result
        }
    }
}

// ── Shared aggregation helpers for Set<ContentFilter> ─────────────────────────
// Used by HomeRepository, CategoryPagingSource, SearchPagingSource, etc.
// Single source of truth — do not re-declare these in individual files.

/** True when the set represents "no filter" (contains ALL or is empty). */
fun Set<ContentFilter>.isAll(): Boolean = isEmpty() || contains(ContentFilter.ALL)

/**
 * TMDB `with_origin_country` value: pipe-joined non-null origin countries.
 * Returns null when [isAll] so standard endpoints are used unchanged.
 */
fun Set<ContentFilter>.originCountryParam(): String? {
    if (isAll()) return null
    val values = mapNotNull { it.originCountry }
    return if (values.isEmpty()) null else values.joinToString("|")
}

/**
 * TMDB `with_genres` value: comma-joined non-null genre IDs.
 * Returns null when [isAll].
 */
fun Set<ContentFilter>.withGenresParam(): String? {
    if (isAll()) return null
    val values = mapNotNull { it.withGenres }
    return if (values.isEmpty()) null else values.joinToString(",")
}

/**
 * TMDB `without_genres` value: comma-joined non-null genre IDs.
 * Returns null when [isAll].
 */
fun Set<ContentFilter>.withoutGenresParam(): String? {
    if (isAll()) return null
    val values = mapNotNull { it.withoutGenres }
    return if (values.isEmpty()) null else values.joinToString(",")
}

/**
 * Stable, sorted cache key string derived from the active filter set.
 * Example: setOf(K_DRAMA, C_DRAMA) → "C_DRAMA_K_DRAMA"
 */
fun Set<ContentFilter>.cacheKey(): String =
    if (isAll()) "ALL" else toList().sortedBy { it.name }.joinToString("_") { it.name }

fun com.yourname.icepacklist.feature.home.domain.Movie.matches(filters: Set<ContentFilter>): Boolean {
    if (filters.isAll()) return true
    val allowedCountries = filters.mapNotNull { it.originCountry }.toSet()
    val allowedGenres = filters.flatMap { it.withGenres?.split(",")?.mapNotNull { id -> id.toIntOrNull() } ?: emptyList() }.toSet()
    val forbiddenGenres = filters.flatMap { it.withoutGenres?.split(",")?.mapNotNull { id -> id.toIntOrNull() } ?: emptyList() }.toSet()

    val hasAllowedCountry = allowedCountries.isEmpty() || originCountry?.any { it in allowedCountries } == true
    val hasAllowedGenre = allowedGenres.isEmpty() || genreIds.any { it in allowedGenres }
    val hasForbiddenGenre = forbiddenGenres.isNotEmpty() && genreIds.any { it in forbiddenGenres }

    return hasAllowedCountry && hasAllowedGenre && !hasForbiddenGenre
}

fun com.yourname.icepacklist.feature.home.domain.TvShow.matches(filters: Set<ContentFilter>): Boolean {
    if (filters.isAll()) return true
    val allowedCountries = filters.mapNotNull { it.originCountry }.toSet()
    val allowedGenres = filters.flatMap { it.withGenres?.split(",")?.mapNotNull { id -> id.toIntOrNull() } ?: emptyList() }.toSet()
    val forbiddenGenres = filters.flatMap { it.withoutGenres?.split(",")?.mapNotNull { id -> id.toIntOrNull() } ?: emptyList() }.toSet()

    val hasAllowedCountry = allowedCountries.isEmpty() || originCountry?.any { it in allowedCountries } == true
    val hasAllowedGenre = allowedGenres.isEmpty() || genreIds.any { it in allowedGenres }
    val hasForbiddenGenre = forbiddenGenres.isNotEmpty() && genreIds.any { it in forbiddenGenres }

    return hasAllowedCountry && hasAllowedGenre && !hasForbiddenGenre
}
