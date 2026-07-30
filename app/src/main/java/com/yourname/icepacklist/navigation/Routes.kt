package com.yourname.icepacklist.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 *
 * Top-level (bottom-bar) routes and nested routes live here so that
 * every call-site imports a single well-known source of truth instead
 * of scattering magic strings across the codebase.
 */
sealed class Routes(val route: String) {

    // ── Bottom-bar destinations ──────────────────────────────────────────

    /** Main movie grid / discovery screen. */
    data object Home : Routes("home")

    /** Global search screen. */
    data object Search : Routes("search")

    /** User preferences / TMDB API key screen. */
    data object Settings : Routes("settings")

    // ── Nested / detail destinations ─────────────────────────────────────

    /**
     * Movie detail screen.
     *
     * Navigation call:  navController.navigate(Routes.Detail.buildRoute(movieId))
     * Route pattern:    "detail/{movieId}"
     */
    data object Detail : Routes("detail/{movieId}") {
        const val ARG_MOVIE_ID = "movieId"

        /** Builds the concrete navigation path for a given [movieId]. */
        fun buildRoute(movieId: Int): String = "detail/$movieId"
    }
}
