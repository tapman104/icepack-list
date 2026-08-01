package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity

@Entity(tableName = "search_cache", primaryKeys = ["query", "page"])
data class SearchCacheEntity(
    val query: String,
    val page: Int,
    val json: String,
    val fetchedAt: Long
)
