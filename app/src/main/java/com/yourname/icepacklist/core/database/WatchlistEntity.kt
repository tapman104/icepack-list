package com.yourname.icepacklist.core.database

import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class MediaType { MOVIE, TV }
enum class WatchStatus { WATCHING, COMPLETED, PAUSED, DROPPED }

class WatchlistConverters {
    @TypeConverter fun fromMediaType(v: MediaType): String = v.name
    @TypeConverter fun toMediaType(v: String): MediaType = MediaType.valueOf(v)
    @TypeConverter fun fromWatchStatus(v: WatchStatus): String = v.name
    @TypeConverter fun toWatchStatus(v: String): WatchStatus = WatchStatus.valueOf(v)
}

@Entity(tableName = "watchlist", primaryKeys = ["id", "mediaType"])
@TypeConverters(WatchlistConverters::class)
data class WatchlistEntity(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val year: String?,
    val status: WatchStatus,
    val addedAt: Long = System.currentTimeMillis()
)
