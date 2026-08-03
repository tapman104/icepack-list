package com.yourname.icepacklist.feature.detail.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.icepacklist.core.ui.ShimmerBox

@Composable
fun DetailScreenShimmer() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Backdrop
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            cornerRadius = 0.dp
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Poster
            ShimmerBox(
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp),
                cornerRadius = 8.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.8f).height(18.dp), cornerRadius = 4.dp)
                // Tagline
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp), cornerRadius = 4.dp)
                // Rating + year + runtime
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp), cornerRadius = 4.dp)
                // Genre chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) {
                        ShimmerBox(modifier = Modifier.width(60.dp).height(24.dp), cornerRadius = 12.dp)
                    }
                }
                // My List button
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(44.dp), cornerRadius = 8.dp)
            }
        }
        
        // Overview lines
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(4) { index ->
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(if (index == 3) 0.6f else 1f)
                        .height(12.dp),
                    cornerRadius = 4.dp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Cast section header placeholder
        ShimmerBox(modifier = Modifier.padding(horizontal = 16.dp).width(80.dp).height(16.dp), cornerRadius = 4.dp)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cast row — circular avatars
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(5) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ShimmerBox(
                        modifier = Modifier.size(64.dp),
                        cornerRadius = 32.dp  // fully circular
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(modifier = Modifier.width(50.dp).height(10.dp), cornerRadius = 4.dp)
                }
            }
        }
    }
}
