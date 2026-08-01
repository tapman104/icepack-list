package com.yourname.icepacklist.feature.person.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Person
import com.yourname.icepacklist.feature.home.domain.PersonImage
import com.yourname.icepacklist.feature.home.domain.PersonMovieCredit
import com.yourname.icepacklist.feature.home.domain.PersonMovieCreditsResponse
import com.yourname.icepacklist.feature.home.domain.PersonImagesResponse
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PersonDetailUiState {
    data object Loading : PersonDetailUiState
    data class Error(val message: String) : PersonDetailUiState
    data class Success(
        val person: Person,
        val movieCredits: List<PersonMovieCredit>,
        val images: List<PersonImage>
    ) : PersonDetailUiState
}

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val apiService: TmdbApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Int = checkNotNull(savedStateHandle[Routes.PersonDetail.ARG_PERSON_ID])

    private val _uiState = MutableStateFlow<PersonDetailUiState>(PersonDetailUiState.Loading)
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PersonDetailUiState.Loading
            try {
                val personDef = async { apiService.getPersonDetails(personId) }
                val creditsDef = async { apiService.getPersonMovieCredits(personId) }
                val imagesDef = async { apiService.getPersonImages(personId) }

                awaitAll(personDef, creditsDef, imagesDef)

                val person = personDef.await()
                val credits = creditsDef.await()
                val images = imagesDef.await()

                val distinctCredits = credits.cast.distinctBy { it.id }.take(10)

                _uiState.value = PersonDetailUiState.Success(
                    person = person,
                    movieCredits = distinctCredits,
                    images = images.profiles
                )
            } catch (e: Exception) {
                _uiState.value = PersonDetailUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
