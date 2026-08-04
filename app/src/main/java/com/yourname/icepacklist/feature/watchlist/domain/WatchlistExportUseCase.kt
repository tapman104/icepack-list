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
            val titleEscaped = if (entity.title.contains(",") || entity.title.contains("\"")) {
                "\"${entity.title.replace("\"", "\"\"")}\""
            } else {
                entity.title
            }
            
            val notesEscaped = if (entity.notes != null && (entity.notes.contains(",") || entity.notes.contains("\"") || entity.notes.contains("\n"))) {
                "\"${entity.notes.replace("\"", "\"\"")}\""
            } else {
                entity.notes ?: ""
            }

            listOf(
                entity.id.toString(),
                titleEscaped,
                entity.mediaType.name,
                entity.year ?: "",
                entity.country ?: "",
                entity.status,
                entity.rating?.toString() ?: "",
                entity.episodesWatched?.toString() ?: "",
                entity.startDate ?: "",
                entity.finishDate ?: "",
                notesEscaped
            ).joinToString(",")
        }
        return buildString {
            appendLine(header)
            rows.forEach { appendLine(it) }
        }
    }
}
