package com.yourname.icepacklist.feature.home.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.data.CategoryPagingSource
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val category = savedStateHandle.get<String>(Routes.CategoryList.CATEGORY_ARG) ?: "popular_movies"
    val items: Flow<PagingData<Any>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { CategoryPagingSource(apiService, category) }
    ).flow.cachedIn(viewModelScope)
}
