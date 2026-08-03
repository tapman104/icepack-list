package com.yourname.icepacklist.feature.home.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yourname.icepacklist.core.datastore.ApiKeyDataStore
import com.yourname.icepacklist.core.datastore.ContentFilter
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.data.CategoryPagingSource
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    private val apiKeyDataStore: ApiKeyDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val category = savedStateHandle.get<String>(Routes.CategoryList.CATEGORY_ARG) ?: "popular_movies"

    // Observe the active home filter from DataStore; recreate the pager on every emission.
    // cachedIn is applied PER inner pager (inside flatMapLatest), NOT on the outer flow.
    // Applying cachedIn on the outer flatMapLatest causes an IllegalStateException when
    // the filter changes and the old PagingData is replayed to a new collector.
    val items: Flow<PagingData<Any>> = apiKeyDataStore.homeContentFilter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = setOf(ContentFilter.ALL)
        )
        .flatMapLatest { filter ->
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = { CategoryPagingSource(apiService, category, filter) }
            ).flow.cachedIn(viewModelScope)
        }
}
