package com.yourname.icepacklist.core.di

import android.content.Context
import androidx.room.Room
import com.yourname.icepacklist.core.database.IcepackDatabase
import com.yourname.icepacklist.core.database.MovieDao
import com.yourname.icepacklist.core.database.RemoteKeyDao
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
        .fallbackToDestructiveMigration()
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
}
