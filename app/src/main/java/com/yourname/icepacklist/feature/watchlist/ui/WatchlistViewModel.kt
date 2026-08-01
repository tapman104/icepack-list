package com.yourname.icepacklist.feature.watchlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchStatus
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository
) : ViewModel() {
    val allItems: StateFlow<List<WatchlistEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getByStatus(status: WatchStatus): Flow<List<WatchlistEntity>> = repository.getByStatus(status)

    fun remove(id: Int, mediaType: MediaType) = viewModelScope.launch {
        repository.remove(id, mediaType)
    }

    fun updateStatus(id: Int, mediaType: MediaType, status: WatchStatus) = viewModelScope.launch {
        repository.updateStatus(id, mediaType, status)
    }
}
