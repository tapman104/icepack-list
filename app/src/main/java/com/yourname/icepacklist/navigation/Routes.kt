package com.yourname.icepacklist.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Search : Routes("search")
    data object Settings : Routes("settings")
    data object ApiKey : Routes("api_key")
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
    data object SeasonEpisodes : Routes("season_episodes/{tvId}/{tvName}/{totalSeasons}") {
        const val ARG_TV_ID = "tvId"
        const val ARG_TV_NAME = "tvName"
        const val ARG_TOTAL_SEASONS = "totalSeasons"
        fun buildRoute(tvId: Int, tvName: String, totalSeasons: Int) = "season_episodes/$tvId/$tvName/$totalSeasons"
    }
    data object FullCast : Routes("full_cast/{mediaId}/{mediaType}") {
        const val ARG_MEDIA_ID = "mediaId"
        const val ARG_MEDIA_TYPE = "mediaType"
        fun buildRoute(mediaId: Int, mediaType: String) = "full_cast/$mediaId/$mediaType"
    }
}
