package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.TvShowDetail
import com.yourname.icepacklist.core.util.formatDate
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TvDetailInfoSection(
    tvShow: TvShowDetail,
    keywords: List<Keyword>,
    onSeasonClick: (Int, String) -> Unit
) {
    // M10 — networkNames map moved into remember; only recalculates when tvShow changes
    val networkNames = remember(tvShow) {
        tvShow.networks.ifEmpty { tvShow.networksList.map { it.name } }
    }

    // M11 — createdBy map+join moved into remember; only recalculates when tvShow changes
    val createdBy = remember(tvShow) {
        tvShow.createdBy.ifEmpty { tvShow.createdByList.map { it.name }.joinToString(", ") }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.details),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(label = "First Air Date", value = formatDate(tvShow.firstAirDate))
        if (!tvShow.lastAirDate.isNullOrBlank()) {
            DetailRow(label = "Last Air Date", value = formatDate(tvShow.lastAirDate))
        }
        if (!tvShow.status.isNullOrBlank()) {
            DetailRow(label = stringResource(R.string.status), value = tvShow.status)
        }
        if (tvShow.originCountry.isNotEmpty()) {
            DetailRow(label = stringResource(R.string.country), value = tvShow.originCountry.joinToString(", "))
        }
        if (!tvShow.originalLanguage.isNullOrBlank()) {
            DetailRow(label = stringResource(R.string.language), value = tvShow.originalLanguage.uppercase())
        }
        if (tvShow.spokenLanguages.isNotEmpty()) {
            DetailRow(
                label = "Spoken Languages",
                value = tvShow.spokenLanguages.joinToString(", ") { it.englishName }
            )
        }
        tvShow.numberOfSeasons?.let { seasons ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeasonClick(tvShow.id, tvShow.name) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Seasons",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(120.dp)
                    )
                    Text(
                        text = "$seasons",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View Seasons", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        tvShow.numberOfEpisodes?.let {
            DetailRow(label = "Episodes", value = "$it")
        }
        if (tvShow.episodeRunTime.isNotEmpty()) {
            DetailRow(label = "Episode Runtime", value = tvShow.episodeRunTime.joinToString(", ") { "${it}m" })
        }
        if (!tvShow.type.isNullOrBlank()) {
            DetailRow(label = "Type", value = tvShow.type)
        }
        // M10 — using pre-computed networkNames
        if (networkNames.isNotEmpty()) {
            DetailRow(label = "Network", value = networkNames.joinToString(", "))
        }
        // M11 — using pre-computed createdBy
        if (createdBy.isNotBlank()) {
            DetailRow(label = "Created By", value = createdBy)
        }

        if (keywords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Keywords",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keywords.take(8).forEach { keyword ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = keyword.name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
