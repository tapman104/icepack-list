package com.yourname.icepacklist.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yourname.icepacklist.feature.home.data.HomeRepository
import com.yourname.icepacklist.feature.home.domain.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    val popularMovies: Flow<PagingData<Movie>> =
        homeRepository.getPopularMovies()
            .cachedIn(viewModelScope)
}
