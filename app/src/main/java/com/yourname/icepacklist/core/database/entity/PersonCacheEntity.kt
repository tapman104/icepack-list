package com.yourname.icepacklist.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_cache")
data class PersonCacheEntity(
    @PrimaryKey val personId: Int,
    val json: String,
    val fetchedAt: Long
)
