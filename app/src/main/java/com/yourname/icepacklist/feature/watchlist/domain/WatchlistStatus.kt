package com.yourname.icepacklist.feature.watchlist.domain

enum class WatchlistStatus(val label: String) {
    WATCHING("Watching"),
    COMPLETED("Completed"),
    PLAN_TO_WATCH("Plan to Watch"),
    PAUSED("Paused"),
    DROPPED("Dropped"),
    REWATCHING("Rewatching")
}
