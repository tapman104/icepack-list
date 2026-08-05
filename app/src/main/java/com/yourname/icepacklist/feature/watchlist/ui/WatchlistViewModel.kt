package com.yourname.icepacklist.feature.watchlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.yourname.icepacklist.feature.watchlist.data.SortOrder
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository
) : ViewModel() {
    val allItems: StateFlow<List<WatchlistEntity>> = repository.getAll(SortOrder.DATE_ADDED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder = _sortOrder.asStateFlow()

    private val _currentStatus = MutableStateFlow("PLAN_TO_WATCH")

    @OptIn(ExperimentalCoroutinesApi::class)
    val visibleItems: StateFlow<List<WatchlistEntity>> = combine(_currentStatus, _sortOrder) { status, sort ->
        status to sort
    }.flatMapLatest { (status, sort) ->
        repository.getByStatus(status, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setStatus(status: String) {
        _currentStatus.value = status
    }

    fun remove(id: Int, mediaType: MediaType) = viewModelScope.launch {
        repository.remove(id, mediaType)
    }

    fun updateStatus(id: Int, mediaType: MediaType, status: String) = viewModelScope.launch {
        repository.updateStatus(id, mediaType, status)
    }
}
