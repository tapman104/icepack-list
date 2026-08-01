package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tv_detail_cache")
data class TvDetailCacheEntity(
    @PrimaryKey val tvId: Int,
    val json: String,
    val fetchedAt: Long
)
