package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Episode
import com.yourname.icepacklist.feature.home.domain.SeasonDetailResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SeasonEpisodesUiState {
    data object Loading : SeasonEpisodesUiState
    data class Error(val message: String) : SeasonEpisodesUiState
    data class Success(val seasonData: SeasonDetailResponse) : SeasonEpisodesUiState
}

@HiltViewModel
class SeasonEpisodesViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val tvId: Int = checkNotNull(savedStateHandle["tvId"])
    val tvName: String = checkNotNull(savedStateHandle["tvName"])
    
    private val _uiState = MutableStateFlow<SeasonEpisodesUiState>(SeasonEpisodesUiState.Loading)
    val uiState: StateFlow<SeasonEpisodesUiState> = _uiState.asStateFlow()
    
    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason.asStateFlow()

    private val _totalSeasons = MutableStateFlow(checkNotNull(savedStateHandle.get<Int>("totalSeasons")))
    val totalSeasons: StateFlow<Int> = _totalSeasons.asStateFlow()

    init {
        loadSeason(1)
    }
    
    fun loadSeason(seasonNumber: Int) {
        _selectedSeason.value = seasonNumber
        viewModelScope.launch {
            _uiState.value = SeasonEpisodesUiState.Loading
            try {
                val data = apiService.getSeasonDetails(tvId, seasonNumber)
                _uiState.value = SeasonEpisodesUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = SeasonEpisodesUiState.Error(e.localizedMessage ?: "Error loading season")
            }
        }
    }
}
