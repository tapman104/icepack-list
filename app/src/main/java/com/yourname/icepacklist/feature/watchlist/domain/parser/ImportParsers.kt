package com.yourname.icepacklist.feature.watchlist.domain.parser

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchlistEntity
import org.json.JSONArray
import javax.inject.Inject

data class ParsedWatchlistItem(
    val title: String,
    val year: String? = null,
    val mediaType: MediaType? = null,
    val rating: Float? = null,
    val status: String? = null,
    val country: String? = null,
    val episodesWatched: Int? = null,
    val startDate: String? = null,
    val finishDate: String? = null,
    val tmdbId: Int? = null
)

interface ImportParser {
    fun canParse(input: String): Boolean
    fun parse(input: String): List<ParsedWatchlistItem>
}

class JsonParser @Inject constructor(
    private val moshi: Moshi
) : ImportParser {
    override fun canParse(input: String): Boolean {
        return try {
            JSONArray(input)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun parse(input: String): List<ParsedWatchlistItem> {
        return try {
            val listType = Types.newParameterizedType(List::class.java, WatchlistEntity::class.java)
            val adapter = moshi.adapter<List<WatchlistEntity>>(listType)
            val entities = adapter.fromJson(input) ?: emptyList()
            entities.map { entity ->
                ParsedWatchlistItem(
                    title = entity.title,
                    year = entity.year,
                    mediaType = entity.mediaType,
                    rating = entity.rating,
                    status = entity.status,
                    country = entity.country,
                    episodesWatched = entity.episodesWatched,
                    startDate = entity.startDate,
                    finishDate = entity.finishDate,
                    tmdbId = entity.id
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class MdlTsvParser @Inject constructor() : ImportParser {
    override fun canParse(input: String): Boolean {
        val lines = input.lines()
        if (lines.isEmpty()) return false
        val header = lines.first().lowercase()
        return header.contains("title") && header.contains("type") && header.contains("score") && 
               (header.contains("country") || header.contains("ep"))
    }

    override fun parse(input: String): List<ParsedWatchlistItem> {
        val lines = input.lines()
        if (lines.size <= 1) return emptyList()

        val headerLine = lines.first()
        val delimiter = when {
            headerLine.contains("\t") -> "\t"
            headerLine.contains("|") -> "|"
            else -> ","
        }

        val headers = headerLine.split(delimiter).map { it.trim().lowercase() }
        val titleIdx = headers.indexOfFirst { it.contains("title") }
        val typeIdx = headers.indexOfFirst { it.contains("type") }
        val yearIdx = headers.indexOfFirst { it.contains("year") }
        val scoreIdx = headers.indexOfFirst { it.contains("score") || it.contains("rating") }
        val countryIdx = headers.indexOfFirst { it.contains("country") }
        val epIdx = headers.indexOfFirst { it.contains("ep") }
        val startedIdx = headers.indexOfFirst { it.contains("started") }
        val finishedIdx = headers.indexOfFirst { it.contains("finished") }

        if (titleIdx == -1) return emptyList()

        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            val cols = line.split(delimiter).map { it.trim() }
            if (cols.size <= titleIdx) return@mapNotNull null

            val title = cols[titleIdx]
            if (title.isBlank()) return@mapNotNull null

            val mediaType = if (typeIdx != -1 && cols.size > typeIdx) {
                if (cols[typeIdx].equals("drama", ignoreCase = true) || cols[typeIdx].equals("tv", ignoreCase = true)) {
                    MediaType.TV
                } else if (cols[typeIdx].equals("movie", ignoreCase = true) || cols[typeIdx].equals("film", ignoreCase = true)) {
                    MediaType.MOVIE
                } else null
            } else null

            val year = if (yearIdx != -1 && cols.size > yearIdx) cols[yearIdx].takeIf { it.isNotBlank() } else null
            val rating = if (scoreIdx != -1 && cols.size > scoreIdx) cols[scoreIdx].toFloatOrNull() else null
            val country = if (countryIdx != -1 && cols.size > countryIdx) cols[countryIdx].takeIf { it.isNotBlank() } else null
            val episodes = if (epIdx != -1 && cols.size > epIdx) cols[epIdx].toIntOrNull() else null
            val startDate = if (startedIdx != -1 && cols.size > startedIdx) cols[startedIdx].takeIf { it.isNotBlank() } else null
            val finishDate = if (finishedIdx != -1 && cols.size > finishedIdx) cols[finishedIdx].takeIf { it.isNotBlank() } else null

            val status = if (finishDate != null) "COMPLETED" else if (startDate != null) "WATCHING" else "PLAN_TO_WATCH"

            ParsedWatchlistItem(
                title = title,
                year = year,
                mediaType = mediaType,
                rating = rating,
                status = status,
                country = country,
                episodesWatched = episodes,
                startDate = startDate,
                finishDate = finishDate
            )
        }
    }
}

class LetterboxdCsvParser @Inject constructor() : ImportParser {
    override fun canParse(input: String): Boolean {
        val lines = input.lines()
        if (lines.isEmpty()) return false
        val header = lines.first().lowercase()
        return header.contains("letterboxd uri") || (header.contains("name") && header.contains("year") && header.contains("rating"))
    }

    override fun parse(input: String): List<ParsedWatchlistItem> {
        val lines = input.lines()
        if (lines.size <= 1) return emptyList()

        val headers = lines.first().split(",").map { it.trim().lowercase() }
        val nameIdx = headers.indexOf("name").takeIf { it != -1 } ?: headers.indexOf("title")
        val yearIdx = headers.indexOf("year")
        val ratingIdx = headers.indexOf("rating")
        val watchedDateIdx = headers.indexOf("watched date")

        if (nameIdx == -1) return emptyList()

        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            // Basic CSV split ignoring commas inside quotes
            val cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim(' ', '"') }
            if (cols.size <= nameIdx) return@mapNotNull null

            val title = cols[nameIdx]
            if (title.isBlank()) return@mapNotNull null

            val year = if (yearIdx != -1 && cols.size > yearIdx) cols[yearIdx].takeIf { it.isNotBlank() } else null
            val rating = if (ratingIdx != -1 && cols.size > ratingIdx) cols[ratingIdx].toFloatOrNull()?.times(2f) else null // Letterboxd is 1-5, convert to 1-10
            val finishDate = if (watchedDateIdx != -1 && cols.size > watchedDateIdx) cols[watchedDateIdx].takeIf { it.isNotBlank() } else null

            ParsedWatchlistItem(
                title = title,
                year = year,
                mediaType = MediaType.MOVIE, // Letterboxd is movies only
                rating = rating,
                status = if (finishDate != null) "COMPLETED" else "WATCHING",
                finishDate = finishDate
            )
        }
    }
}

class PlainListParser @Inject constructor() : ImportParser {
    override fun canParse(input: String): Boolean {
        // Fallback parser, accepts if it's just lines of text
        val lines = input.lines().filter { it.isNotBlank() }
        return lines.isNotEmpty() && !JsonParser(Moshi.Builder().build()).canParse(input)
    }

    override fun parse(input: String): List<ParsedWatchlistItem> {
        val regex = "^(.*?)(?:\\s*\\((\\d{4})\\))?$".toRegex()
        return input.lines().filter { it.isNotBlank() }.map { line ->
            val match = regex.find(line.trim())
            val title = match?.groupValues?.get(1)?.trim() ?: line.trim()
            val year = match?.groupValues?.get(2)?.takeIf { it.isNotBlank() }
            
            ParsedWatchlistItem(
                title = title,
                year = year,
                status = "PLAN_TO_WATCH"
            )
        }
    }
}
