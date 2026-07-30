package com.yourname.icepacklist.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MovieEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IcepackDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
