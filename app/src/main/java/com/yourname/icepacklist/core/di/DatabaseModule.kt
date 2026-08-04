package com.yourname.icepacklist.core.di

import android.content.Context
import androidx.room.Room
import com.yourname.icepacklist.core.database.IcepackDatabase
import com.yourname.icepacklist.core.database.MovieDao
import com.yourname.icepacklist.core.database.RemoteKeyDao
import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideIcepackDatabase(
        @ApplicationContext context: Context
    ): IcepackDatabase {
        return Room.databaseBuilder(
            context,
            IcepackDatabase::class.java,
            "icepack_db"
        )
        .addMigrations(IcepackDatabase.MIGRATION_4_5, IcepackDatabase.MIGRATION_5_6, IcepackDatabase.MIGRATION_6_7)
        .build()
    }

    @Provides
    @Singleton
    fun provideMovieDao(database: IcepackDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(database: IcepackDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }

    @Provides
    @Singleton
    fun provideWatchlistDao(database: IcepackDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    @Singleton
    fun provideHomeListCacheDao(database: IcepackDatabase): HomeListCacheDao =
        database.homeListCacheDao()

    @Provides
    @Singleton
    fun provideMovieDetailCacheDao(database: IcepackDatabase): MovieDetailCacheDao =
        database.movieDetailCacheDao()

    @Provides
    @Singleton
    fun provideTvDetailCacheDao(database: IcepackDatabase): TvDetailCacheDao =
        database.tvDetailCacheDao()

    @Provides
    @Singleton
    fun providePersonCacheDao(database: IcepackDatabase): PersonCacheDao =
        database.personCacheDao()

    @Provides
    @Singleton
    fun provideSearchCacheDao(database: IcepackDatabase): SearchCacheDao =
        database.searchCacheDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: IcepackDatabase): SearchHistoryDao =
        database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideHiddenItemDao(database: IcepackDatabase): HiddenItemDao =
        database.hiddenItemDao()
}
