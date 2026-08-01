package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_list_cache")
data class HomeListCacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String,
    val fetchedAt: Long
)
