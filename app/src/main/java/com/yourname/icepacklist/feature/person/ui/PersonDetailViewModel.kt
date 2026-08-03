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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.feature.watchlist.data.WatchlistRepository

data class PersonUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val person: PersonDetail? = null,
    val knownFor: List<CombinedCreditsCast> = emptyList(),
    val dramas: List<CombinedCreditsCast> = emptyList(),
    val movies: List<CombinedCreditsCast> = emptyList(),
    val tvShows: List<CombinedCreditsCast> = emptyList(),
    val images: List<PersonImage> = emptyList(),
    val nativeName: String = "",
    val nationality: String = "",
    val age: Int? = null
)

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: DetailRepository,
    private val watchlistRepository: WatchlistRepository,
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
                coroutineScope {
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

                    val nativeName = personDetail.alsoKnownAs
                        .firstOrNull { it.any { c -> c.code in 0xAC00..0xD7A3 } }  // first Korean-script entry
                        ?: personDetail.alsoKnownAs.getOrNull(1)
                        ?: ""

                    val nationality = when {
                        personDetail.placeOfBirth?.contains("Korea", ignoreCase = true) == true -> "South Korean"
                        personDetail.placeOfBirth?.contains("Japan", ignoreCase = true) == true -> "Japanese"
                        personDetail.placeOfBirth?.contains("China", ignoreCase = true) == true -> "Chinese"
                        personDetail.placeOfBirth?.contains("Hong Kong", ignoreCase = true) == true -> "Hong Kongese"
                        personDetail.placeOfBirth?.contains("Taiwan", ignoreCase = true) == true -> "Taiwanese"
                        else -> personDetail.placeOfBirth?.substringAfterLast(",")?.trim() ?: ""
                    }

                    val age = personDetail.birthday?.let {
                        try {
                            val birth = LocalDate.parse(it)
                            Period.between(birth, LocalDate.now()).years
                        } catch (e: Exception) { null }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            person = personDetail,
                            knownFor = knownFor,
                            dramas = dramas,
                            movies = allMovies,
                            tvShows = tvShows,
                            images = images,
                            nativeName = nativeName,
                            nationality = nationality,
                            age = age
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            } catch (e: HttpException) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    fun addToWatchlist(credit: CombinedCreditsCast) {
        viewModelScope.launch {
            val entity = WatchlistEntity(
                id = credit.id,
                mediaType = if (credit.mediaType == "tv") MediaType.TV else MediaType.MOVIE,
                title = credit.displayTitle,
                posterPath = credit.posterPath ?: "",
                voteAverage = 0.0,
                year = credit.displayYear,
                status = "PLAN_TO_WATCH"
            )
            watchlistRepository.add(entity)
        }
    }
}
