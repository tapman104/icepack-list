package com.yourname.icepacklist.core.di

import com.yourname.icepacklist.feature.home.data.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.yourname.icepacklist.core.network.TmdbApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHomeRepository(apiService: TmdbApiService): HomeRepository {
        return HomeRepository(apiService)
    }
}
