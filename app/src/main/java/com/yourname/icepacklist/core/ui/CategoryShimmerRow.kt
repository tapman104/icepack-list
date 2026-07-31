package com.yourname.icepacklist.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryShimmerRow() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            ShimmerBox(modifier = Modifier.width(120.dp), height = 20.dp)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(6) {
                ShimmerBox(modifier = Modifier.width(120.dp), height = 180.dp)
            }
        }
    }
}
