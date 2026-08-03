package com.yourname.icepacklist.feature.person.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.icepacklist.feature.detail.data.DetailRepository
import com.yourname.icepacklist.feature.home.domain.PersonDetail
import com.yourname.icepacklist.feature.home.domain.PersonImage
import com.yourname.icepacklist.feature.home.domain.CombinedCreditsCast
import com.yourname.icepacklist.feature.home.domain.CombinedCreditsResponse
import com.yourname.icepacklist.feature.home.domain.PersonImagesResponse
import com.yourname.icepacklist.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class PersonUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val person: PersonDetail? = null,
    val knownFor: List<CombinedCreditsCast> = emptyList(),
    val dramas: List<CombinedCreditsCast> = emptyList(),
    val movies: List<CombinedCreditsCast> = emptyList(),
    val tvShows: List<CombinedCreditsCast> = emptyList(),
    val images: List<PersonImage> = emptyList()
)

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: DetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Int = checkNotNull(savedStateHandle[Routes.PersonDetail.ARG_PERSON_ID])

    private val _uiState = MutableStateFlow(PersonUiState())
    val uiState: StateFlow<PersonUiState> = _uiState.asStateFlow()

    init {
        loadPerson()
    }

    fun retry() = loadPerson()

    private fun loadPerson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }
            try {
                val results = awaitAll(
                    async { repository.getPersonDetail(personId) },
                    async { repository.getPersonCombinedCredits(personId) },
                    async { repository.getPersonImages(personId) }
                )
                
                val personDetail = results[0] as PersonDetail
                val combinedCredits = (results[1] as CombinedCreditsResponse).cast
                    .sortedByDescending { it.releaseDate ?: it.firstAirDate }
                val images = (results[2] as PersonImagesResponse).profiles

                val knownFor = combinedCredits.take(10)

                // Split by media_type, then by genre for dramas
                val allMovies = combinedCredits.filter { it.mediaType == "movie" }
                val allTv = combinedCredits.filter { it.mediaType == "tv" }

                // Korean dramas: genre_id 18 (Drama) in TV credits
                val dramas = allTv.filter { 18 in it.genreIds }
                val tvShows = allTv.filter { 18 !in it.genreIds }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        person = personDetail,
                        knownFor = knownFor,
                        dramas = dramas,
                        movies = allMovies,
                        tvShows = tvShows,
                        images = images
                    )
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            } catch (e: HttpException) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }
}
