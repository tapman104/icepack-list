package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.icepacklist.core.database.MediaType
import com.yourname.icepacklist.core.database.WatchlistEntity
import com.yourname.icepacklist.feature.watchlist.domain.WatchlistStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// M7 — statuses list moved to top-level val; it is constant and never needs to be re-allocated
// inside the composable body on every recomposition
private val WATCHLIST_STATUSES: List<Pair<WatchlistStatus, ImageVector>> = listOf(
    WatchlistStatus.WATCHING to Icons.Default.Visibility,
    WatchlistStatus.COMPLETED to Icons.Default.CheckCircle,
    WatchlistStatus.PLAN_TO_WATCH to Icons.Default.Schedule,
    WatchlistStatus.PAUSED to Icons.Default.PauseCircle,
    WatchlistStatus.DROPPED to Icons.Default.Cancel,
    WatchlistStatus.REWATCHING to Icons.Default.Refresh
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyListSheet(
    entry: WatchlistEntity,
    mediaType: MediaType,
    totalEpisodes: Int? = null,
    onSave: (WatchlistEntity) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember { mutableStateOf(entry.status) }
    var rating by remember { mutableStateOf(entry.rating) }
    var startDate by remember { mutableStateOf(entry.startDate) }
    var finishDate by remember { mutableStateOf(entry.finishDate) }
    var notes by remember { mutableStateOf(entry.notes ?: "") }
    var episodesWatched by remember { mutableStateOf(entry.episodesWatched ?: 0) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showFinishDatePicker by remember { mutableStateOf(false) }

    // M8 — Save button gradient captured in remember; not re-allocated on every state change
    val saveButtonGradient = remember {
        Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFFA55EAA)))
    }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("MY LIST", color = Color(0xFFA55EAA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(entry.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Status Grid
            Text("STATUS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // M7 — using top-level constant list; no allocation here
                WATCHLIST_STATUSES.forEach { (ws, icon) ->
                    val isSelected = status == ws.name
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFA55EAA) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color.Gray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { status = ws.name }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(ws.label, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RATING", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val currentRating = rating
                Text(if (currentRating != null) "${currentRating.toInt()}/10" else "Not rated", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..10) {
                    val isFilled = rating != null && i <= rating!!
                    Icon(
                        imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Rate $i",
                        tint = if (isFilled) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { rating = i.toFloat() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("START DATE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startDate?.let { formatIsoDateToDisplay(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("dd MMM yyyy", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.Gray,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("FINISH DATE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = finishDate?.let { formatIsoDateToDisplay(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("dd MMM yyyy", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().clickable { showFinishDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.Gray,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // TV Episodes
            if (mediaType == MediaType.TV) {
                Text("EPISODE PROGRESS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (episodesWatched > 0) episodesWatched-- }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Color.White)
                    }
                    Text(
                        text = "$episodesWatched / ${totalEpisodes ?: "?"}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { if (totalEpisodes == null || episodesWatched < totalEpisodes) episodesWatched++ }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Notes
            Text("NOTES", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Quick thought about this title...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA55EAA),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(
                    onClick = {
                        onRemove()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove", color = Color(0xFFE50914))
                }
                Button(
                    onClick = {
                        val updated = entry.copy(
                            status = status,
                            rating = rating,
                            startDate = startDate,
                            finishDate = finishDate,
                            notes = notes.takeIf { it.isNotBlank() },
                            episodesWatched = episodesWatched
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                saveButtonGradient, // M8 — remembered gradient
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓ Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerModal(
            onDateSelected = { dateMillis ->
                if (dateMillis != null) startDate = formatMillisToIso(dateMillis)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showFinishDatePicker) {
        DatePickerModal(
            onDateSelected = { dateMillis ->
                if (dateMillis != null) finishDate = formatMillisToIso(dateMillis)
                showFinishDatePicker = false
            },
            onDismiss = { showFinishDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatIsoDateToDisplay(isoDate: String): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = isoFormat.parse(isoDate) ?: return isoDate
        val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        displayFormat.format(date)
    } catch (e: Exception) {
        isoDate
    }
}

private fun formatMillisToIso(millis: Long): String {
    val date = Date(millis)
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return isoFormat.format(date)
}
