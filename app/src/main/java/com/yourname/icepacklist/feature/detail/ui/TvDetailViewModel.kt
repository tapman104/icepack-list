package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.core.ui.UiState
import com.yourname.icepacklist.feature.home.domain.CreditsResponse
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvDetailViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvId: Int = checkNotNull(savedStateHandle[Routes.TvDetail.ARG_TV_ID])

    private val _tvShowState = MutableStateFlow<UiState<TvShowDetail>>(UiState.Loading)
    val tvShowState: StateFlow<UiState<TvShowDetail>> = _tvShowState.asStateFlow()

    private val _creditsState = MutableStateFlow<UiState<CreditsResponse>>(UiState.Loading)
    val creditsState: StateFlow<UiState<CreditsResponse>> = _creditsState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _tvShowState.value = UiState.Loading
            _creditsState.value = UiState.Loading
            
            val detailDef = async { runCatching { apiService.getTvShowDetails(tvId) } }
            val creditsDef = async { runCatching { apiService.getTvShowCredits(tvId) } }
            
            val detailResult = detailDef.await()
            if (detailResult.isSuccess) {
                _tvShowState.value = UiState.Success(detailResult.getOrThrow())
            } else {
                _tvShowState.value = UiState.Error(detailResult.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
            
            val creditsResult = creditsDef.await()
            if (creditsResult.isSuccess) {
                _creditsState.value = UiState.Success(creditsResult.getOrThrow())
            } else {
                _creditsState.value = UiState.Error(creditsResult.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }
}
