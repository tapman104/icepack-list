package com.yourname.icepacklist.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Placeholder Search screen — will be fleshed out in a later stage.
 */
@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Search",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
