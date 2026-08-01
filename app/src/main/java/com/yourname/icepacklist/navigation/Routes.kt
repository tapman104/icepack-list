package com.yourname.icepacklist.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Settings : Routes("settings")
    data object Watchlist : Routes("watchlist")
    data object Detail : Routes("detail/{movieId}") {
        const val ARG_MOVIE_ID = "movieId"
        fun buildRoute(movieId: Int): String = "detail/$movieId"
    }
    data object TvDetail : Routes("tv_detail/{tvId}") {
        const val ARG_TV_ID = "tvId"
        fun buildRoute(tvId: Int): String = "tv_detail/$tvId"
    }
    data object CategoryList : Routes("category_list/{category}") {
        const val CATEGORY_ARG = "category"
        fun createRoute(category: String) = "category_list/$category"
    }
    data object PersonDetail : Routes("person_detail/{personId}") {
        const val ARG_PERSON_ID = "personId"
        fun createRoute(personId: Int) = "person_detail/$personId"
    }
}
