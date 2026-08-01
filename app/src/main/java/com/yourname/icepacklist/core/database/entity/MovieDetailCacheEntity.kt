package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_detail_cache")
data class MovieDetailCacheEntity(
    @PrimaryKey val movieId: Int,
    val json: String,
    val fetchedAt: Long
)
