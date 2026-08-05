package com.yourname.icepacklist.feature.watchlist.data

sealed class ImportState {
    object Idle : ImportState()

    data class Progress(
        val current: Int,
        val total: Int,
        val currentTitle: String,
        val phase: ImportPhase = ImportPhase.Resolving,
    ) : ImportState()

    data class Success(
        val importedCount: Int,
        val skippedTitles: List<String>,
    ) : ImportState()

    data class Error(val message: String) : ImportState()
}

enum class ImportPhase {
    Parsing,
    Resolving,
    Saving,
}
