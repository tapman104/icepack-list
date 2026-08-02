package com.yourname.icepacklist.core.database

import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonClass

enum class MediaType { MOVIE, TV }

class WatchlistConverters {
    @TypeConverter fun fromMediaType(v: MediaType): String = v.name
    @TypeConverter fun toMediaType(v: String): MediaType = MediaType.valueOf(v)
}

@Entity(tableName = "watchlist", primaryKeys = ["id", "mediaType"])
@TypeConverters(WatchlistConverters::class)
@JsonClass(generateAdapter = true)
data class WatchlistEntity(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val year: String?,
    val status: String = "PLAN_TO_WATCH",
    val rating: Float? = null,
    val startDate: String? = null,
    val finishDate: String? = null,
    val notes: String? = null,
    val episodesWatched: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
)
