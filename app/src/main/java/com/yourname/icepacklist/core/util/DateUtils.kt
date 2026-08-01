package com.yourname.icepacklist.core.util

import java.text.SimpleDateFormat
import java.util.Locale

private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val parsed = inputFormat.parse(dateStr)
        if (parsed != null) outputFormat.format(parsed) else ""
    } catch (e: Exception) {
        ""
    }
}
