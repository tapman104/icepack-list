package com.yourname.icepacklist.core.database

import androidx.room.Entity

@Entity(tableName = "watchlist", primaryKeys = ["id", "mediaType"])
data class WatchlistEntity(
    val id: Int,
    val mediaType: String,        // "movie" or "tv"
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val year: String?,
    val status: String,           // "watching", "completed", "paused", "dropped"
    val addedAt: Long = System.currentTimeMillis()
)
