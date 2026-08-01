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
    version = 5,
    exportSchema = false
)
@TypeConverters(WatchlistConverters::class)
abstract class IcepackDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watchlist ADD COLUMN rating REAL")
                db.execSQL("ALTER TABLE watchlist ADD COLUMN startDate TEXT")
                db.execSQL("ALTER TABLE watchlist ADD COLUMN finishDate TEXT")
                db.execSQL("ALTER TABLE watchlist ADD COLUMN notes TEXT")
                db.execSQL("ALTER TABLE watchlist ADD COLUMN episodesWatched INTEGER")
            }
        }
    }
    
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun watchlistDao(): WatchlistDao
    
    abstract fun homeListCacheDao(): HomeListCacheDao
    abstract fun movieDetailCacheDao(): MovieDetailCacheDao
    abstract fun tvDetailCacheDao(): TvDetailCacheDao
    abstract fun personCacheDao(): PersonCacheDao
    abstract fun searchCacheDao(): SearchCacheDao
}
