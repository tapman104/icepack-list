package com.yourname.icepacklist.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.TypeConverters
import com.yourname.icepacklist.core.database.entity.*
import com.yourname.icepacklist.core.database.dao.*

@Database(
    entities = [
        MovieEntity::class, RemoteKeyEntity::class, WatchlistEntity::class,
        HomeListCacheEntity::class, MovieDetailCacheEntity::class,
        TvDetailCacheEntity::class, PersonCacheEntity::class, SearchCacheEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(WatchlistConverters::class)
abstract class IcepackDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun watchlistDao(): WatchlistDao
    
    abstract fun homeListCacheDao(): HomeListCacheDao
    abstract fun movieDetailCacheDao(): MovieDetailCacheDao
    abstract fun tvDetailCacheDao(): TvDetailCacheDao
    abstract fun personCacheDao(): PersonCacheDao
    abstract fun searchCacheDao(): SearchCacheDao
}
