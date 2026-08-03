package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.icepacklist.R
import com.yourname.icepacklist.feature.home.domain.Keyword
import com.yourname.icepacklist.feature.home.domain.MovieDetail
import com.yourname.icepacklist.core.util.formatDate
import java.util.Locale
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailInfoSection(
    movie: MovieDetail,
    keywords: List<Keyword>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.details),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(label = stringResource(R.string.release_date), value = formatDate(movie.releaseDate))
        if (!movie.status.isNullOrBlank()) {
            DetailRow(label = stringResource(R.string.status), value = movie.status)
        }
        if (movie.originCountry.isNotEmpty()) {
            DetailRow(label = stringResource(R.string.country), value = movie.originCountry.joinToString(", "))
        }
        if (!movie.originalLanguage.isNullOrBlank()) {
            DetailRow(label = stringResource(R.string.language), value = movie.originalLanguage.uppercase())
        }
        if (movie.spokenLanguages.isNotEmpty()) {
            DetailRow(
                label = "Spoken Languages",
                value = movie.spokenLanguages.joinToString(", ") { it.englishName }
            )
        }
        movie.runtime?.takeIf { it > 0 }?.let {
            DetailRow(label = stringResource(R.string.runtime), value = "${it / 60}h ${it % 60}m")
        }
        movie.budget?.takeIf { it > 0 }?.let {
            val formatted = java.text.NumberFormat.getCurrencyInstance(Locale.US).apply { 
                maximumFractionDigits = 0 
            }.format(it)
            DetailRow(label = stringResource(R.string.detail_budget), value = formatted)
        }
        movie.revenue?.takeIf { it > 0 }?.let {
            val formatted = java.text.NumberFormat.getCurrencyInstance(Locale.US).apply { 
                maximumFractionDigits = 0 
            }.format(it)
            DetailRow(label = stringResource(R.string.detail_revenue), value = formatted)
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
