package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_items")
data class HiddenItemEntity(
    @PrimaryKey val id: Int,
    val mediaType: String, // "MOVIE" or "TV"
    val title: String,
    val hiddenAt: Long = System.currentTimeMillis()
)
