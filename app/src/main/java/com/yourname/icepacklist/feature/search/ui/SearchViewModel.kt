package com.yourname.icepacklist.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import com.yourname.icepacklist.feature.search.data.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.yourname.icepacklist.core.datastore.ContentFilter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val apiKeyDataStore: com.yourname.icepacklist.core.datastore.ApiKeyDataStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // DataStore now emits Set<ContentFilter> directly — no mapping needed
    private val searchConfig = apiKeyDataStore.searchContentFilter
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf(ContentFilter.ALL))

    val searchHistory: StateFlow<List<com.yourname.icepacklist.core.database.entity.SearchHistoryEntity>> = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: Flow<PagingData<MultiSearchResult>> = _searchQuery
        .debounce(300)
        .filter { it.isNotBlank() }
        .combine(searchConfig) { query, filter ->
            query to filter
        }
        .flatMapLatest { (query, filter) ->
            if (query.isNotBlank()) {
                repository.addSearchQuery(query.trim())
            }
            repository.searchMulti(query, filter)
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun removeSearchHistoryItem(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
