package com.yourname.icepacklist.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val movie: MovieDetail) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: DetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle[Routes.Detail.ARG_MOVIE_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val result = repository.getMovieDetails(movieId)
            result.onSuccess { movie ->
                _uiState.value = DetailUiState.Success(movie)
            }.onFailure { error ->
                _uiState.value = DetailUiState.Error(error.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
