package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.icepacklist.R
import com.yourname.icepacklist.core.ui.MovieCard
import com.yourname.icepacklist.feature.home.domain.Movie
import com.yourname.icepacklist.feature.home.domain.Review

@Composable
fun DetailRecommendationsSection(
    similar: List<Movie>,
    reviews: List<Review>,
    onMovieClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (similar.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.you_may_also_like),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(modifier = Modifier.height(180.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(similar, key = { it.id }) { simMovie ->
                    MovieCard(movie = simMovie, onClick = { onMovieClick(simMovie.id) })
                }
            }
        }

        if (reviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Reviews",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                reviews.take(3).forEach { review ->
                    ReviewItem(review = review)
                }
            }
        }
    }
}
