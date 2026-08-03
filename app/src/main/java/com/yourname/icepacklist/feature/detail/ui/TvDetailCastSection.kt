package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.CreditsResponse

@Composable
fun TvDetailCastSection(
    credits: CreditsResponse,
    mediaId: Int,
    mediaType: String,
    onPersonClick: (Int) -> Unit,
    onFullCastClick: (Int, String) -> Unit
) {
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
                            com.yourname.icepacklist.feature.detail.ui.CreditsHolder.credits = credits
                            onFullCastClick(mediaId, mediaType)
                        }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(credits.cast.take(15), key = { it.id }) { person ->
                    CastItemCard(
                        person = person,
                        onClick = { onPersonClick(person.id) }
                    )
                }
            }
        }

        val crewJobs = listOf("Director", "Writer", "Screenplay", "Creator", "Executive Producer")
        val crewList = credits.crew.filter { it.job in crewJobs }.take(6)
        if (crewList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Crew",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.height(92.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(crewList, key = { it.id.toString() + it.job }) { person ->
                    CrewItemCard(
                        person = person,
                        onClick = { onPersonClick(person.id) }
                    )
                }
            }
        }
    }
}
