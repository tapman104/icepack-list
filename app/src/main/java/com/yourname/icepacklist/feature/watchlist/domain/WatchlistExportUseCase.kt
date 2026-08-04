package com.yourname.icepacklist.feature.watchlist.domain

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.yourname.icepacklist.core.database.WatchlistDao
import com.yourname.icepacklist.core.database.WatchlistEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WatchlistExportUseCase @Inject constructor(
    private val dao: WatchlistDao,
    private val moshi: Moshi
) {
    suspend fun exportToJson(): String {
        val list = dao.getAll().first()
        val listType = Types.newParameterizedType(List::class.java, WatchlistEntity::class.java)
        val adapter = moshi.adapter<List<WatchlistEntity>>(listType)
        return adapter.toJson(list)
    }

    suspend fun exportToCsv(): String {
        val list = dao.getAll().first()
        val header = "TMDB_ID,Title,MediaType,Year,Country,Status,UserRating,EpisodesWatched,StartDate,FinishDate,Notes"
        val rows = list.map { entity ->
            listOf(
                entity.id.toString(),
                escapeCsv(entity.title),
                entity.mediaType.name,
                escapeCsv(entity.year),
                escapeCsv(entity.country),
                escapeCsv(entity.status),
                escapeCsv(entity.rating?.toString()),
                escapeCsv(entity.episodesWatched?.toString()),
                escapeCsv(entity.startDate),
                escapeCsv(entity.finishDate),
                escapeCsv(entity.notes)
            ).joinToString(",")
        }
        return buildString {
            appendLine(header)
            rows.forEach { appendLine(it) }
        }
    }

    private fun escapeCsv(value: String?): String {
        if (value == null) return ""
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
