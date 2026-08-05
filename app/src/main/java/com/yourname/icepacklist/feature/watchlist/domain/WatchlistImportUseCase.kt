package com.yourname.icepacklist.feature.watchlist.domain

import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.core.network.TmdbApiService
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.MultiSearchResult
import com.yourname.icepacklist.feature.home.domain.TvShow
import com.yourname.icepacklist.feature.watchlist.data.ImportPhase
import com.yourname.icepacklist.feature.watchlist.data.ImportState
import com.yourname.icepacklist.feature.watchlist.domain.parser.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class WatchlistImportUseCase @Inject constructor(
    private val dao: WatchlistDao,
    private val tmdbApi: TmdbApiService,
    private val jsonParser: JsonParser,
    private val mdlTsvParser: MdlTsvParser,
    private val letterboxdCsvParser: LetterboxdCsvParser,
    private val plainListParser: PlainListParser
) {
    fun invoke(input: String): Flow<ImportState> = flow {
        try {
            val parsers = listOf(jsonParser, mdlTsvParser, letterboxdCsvParser, plainListParser)
            val parser = parsers.firstOrNull { it.canParse(input) }
            
            if (parser == null) {
                emit(ImportState.Error("Unsupported file format"))
                return@flow
            }

            val parsedItems = parser.parse(input)
            if (parsedItems.isEmpty()) {
                emit(ImportState.Error("No valid items found in the file"))
                return@flow
            }

            val total = parsedItems.size
            var importedCount = 0
            val skippedTitles = mutableListOf<String>()

            for (i in parsedItems.indices) {
                val item = parsedItems[i]
                try {
                    val resolvedEntity = resolveAndMapToEntity(item)
                    if (resolvedEntity != null) {
                        dao.insert(resolvedEntity)
                        importedCount++
                    } else {
                        skippedTitles.add(item.title)
                    }
                } catch (e: Exception) {
                    skippedTitles.add(item.title)
                }
                
                emit(ImportState.Progress(i + 1, total, item.title, ImportPhase.Resolving))
            }

            emit(ImportState.Success(importedCount, skippedTitles))
            
        } catch (e: Exception) {
            e.printStackTrace()
            emit(ImportState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun resolveAndMapToEntity(item: ParsedWatchlistItem): WatchlistEntity? {
        if (item.tmdbId != null && item.mediaType != null) {
            return WatchlistEntity(
                id = item.tmdbId,
                mediaType = item.mediaType,
                title = item.title,
                posterPath = null,
                voteAverage = 0.0,
                year = item.year,
                status = item.status ?: "PLAN_TO_WATCH",
                rating = item.rating,
                startDate = item.startDate,
                finishDate = item.finishDate,
                episodesWatched = item.episodesWatched,
                country = item.country
            )
        }

        // Rate limit protection (~4 requests per sec max)
        delay(260)
        
        val results = if (item.mediaType == MediaType.MOVIE) {
            tmdbApi.searchMovies(item.title).results.map { it to MediaType.MOVIE }
        } else if (item.mediaType == MediaType.TV) {
            tmdbApi.searchTvShows(item.title).results.map { it to MediaType.TV }
        } else {
            val multi = tmdbApi.searchMulti(item.title).results
            multi.mapNotNull { 
                if (it.mediaType == "movie") it to MediaType.MOVIE 
                else if (it.mediaType == "tv") it to MediaType.TV 
                else null 
            }
        }

        if (results.isEmpty()) return null

        var bestMatch = results.first()
        
        if (item.year != null) {
            val yearInt = item.year.toIntOrNull()
            if (yearInt != null) {
                val yearMatches = results.filter { result ->
                    val resultYearStr = (result.first as? Movie)?.releaseDate?.take(4)
                        ?: (result.first as? TvShow)?.firstAirDate?.take(4)
                        ?: (result.first as? MultiSearchResult)?.releaseDate?.take(4)
                        ?: (result.first as? MultiSearchResult)?.firstAirDate?.take(4)
                        
                    val resultYear = resultYearStr?.toIntOrNull()
                    resultYear != null && kotlin.math.abs(resultYear - yearInt) <= 1
                }
                
                if (yearMatches.isNotEmpty()) {
                    bestMatch = yearMatches.first()
                } else {
                    bestMatch = results.first()
                }
            } else {
                bestMatch = results.first()
            }
        } else {
            bestMatch = results.first()
        }

        val bestItem = bestMatch.first
        val bestType = bestMatch.second

        val id = (bestItem as? Movie)?.id
            ?: (bestItem as? TvShow)?.id
            ?: (bestItem as? MultiSearchResult)?.id
            ?: return null

        val title = (bestItem as? Movie)?.title
            ?: (bestItem as? TvShow)?.name
            ?: (bestItem as? MultiSearchResult)?.title
            ?: (bestItem as? MultiSearchResult)?.name
            ?: item.title

        val posterPath = (bestItem as? Movie)?.posterPath
            ?: (bestItem as? TvShow)?.posterPath
            ?: (bestItem as? MultiSearchResult)?.posterPath

        val voteAverage = (bestItem as? Movie)?.voteAverage
            ?: (bestItem as? TvShow)?.voteAverage
            ?: (bestItem as? MultiSearchResult)?.voteAverage
            ?: 0.0

        val yearStr = (bestItem as? Movie)?.releaseDate?.take(4)
            ?: (bestItem as? TvShow)?.firstAirDate?.take(4)
            ?: (bestItem as? MultiSearchResult)?.releaseDate?.take(4)
            ?: (bestItem as? MultiSearchResult)?.firstAirDate?.take(4)

        return WatchlistEntity(
            id = id,
            mediaType = bestType,
            title = title,
            posterPath = posterPath,
            voteAverage = voteAverage,
            year = yearStr,
            status = item.status ?: "PLAN_TO_WATCH",
            rating = item.rating,
            startDate = item.startDate,
            finishDate = item.finishDate,
            episodesWatched = item.episodesWatched,
            country = item.country
        )
    }
}
