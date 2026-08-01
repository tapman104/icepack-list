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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: Flow<PagingData<MultiSearchResult>> = _searchQuery
        .debounce(300)
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            repository.searchMulti(query)
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
