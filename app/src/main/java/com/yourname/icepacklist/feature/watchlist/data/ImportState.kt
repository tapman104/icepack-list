package com.yourname.icepacklist.feature.watchlist.data

sealed class ImportState {
    data class Progress(val current: Int, val total: Int) : ImportState()
    data class Success(val imported: Int, val skipped: Int) : ImportState()
    data class Error(val message: String?) : ImportState()
}
