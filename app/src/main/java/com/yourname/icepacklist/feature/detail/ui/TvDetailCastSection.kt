package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.CreditsResponse

// H9 — crewJobs moved to top-level constant Set for O(1) lookup; never allocated inside the composable
private val CREW_JOBS = setOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")

@Composable
fun TvDetailCastSection(
    credits: CreditsResponse,
    mediaId: Int,
    mediaType: String,
    onPersonClick: (Int) -> Unit,
    onFullCastClick: (Int, String) -> Unit
) {
    // H9 — crew filter moved into remember; only recalculates when credits reference changes
    val filteredCrew = remember(credits) {
        credits.crew.filter { it.job in CREW_JOBS }.take(6)
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (credits.cast.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cast),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "View All",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .clickable {
                            CreditsHolder.credits = credits
                            onFullCastClick(mediaId, mediaType)
                        }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Cast id is unique per list — plain Int key
                items(credits.cast.take(15), key = { it.id }) { person ->
                    CastItemCard(
                        person = person,
                        onClick = { onPersonClick(person.id) }
                    )
                }
            }
        }

        // H9 — using pre-computed filteredCrew
        if (filteredCrew.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Crew",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // M2 — compound Int key; no string allocation
                items(filteredCrew, key = { it.id * 31 + (it.job?.hashCode() ?: 0) }) { person ->
                    CrewItemCard(
                        person = person,
                        onClick = { onPersonClick(person.id) }
                    )
                }
            }
        }
    }
}
