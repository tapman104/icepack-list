package com.yourname.icepacklist.feature.person.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourname.icepacklist.core.ui.ShimmerBox

@Composable
fun PersonDetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile image + name
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(100.dp),
                cornerRadius = 50.dp  // circular
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(modifier = Modifier.width(140.dp).height(18.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.width(100.dp).height(12.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.width(80.dp).height(12.dp), cornerRadius = 4.dp)
            }
        }
        
        // Info rows — 6 rows matching PersonInfoSection
        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.size(20.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.width(90.dp).height(12.dp), cornerRadius = 4.dp)
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp), cornerRadius = 4.dp)
            }
        }
        
        // Biography lines
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(modifier = Modifier.width(100.dp).height(16.dp), cornerRadius = 4.dp)
            Spacer(modifier = Modifier.height(4.dp))
            repeat(5) { index ->
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(if (index == 4) 0.5f else 1f).height(12.dp),
                    cornerRadius = 4.dp
                )
            }
        }
        
        // Section header + 3 filmography card placeholders
        ShimmerBox(modifier = Modifier.width(80.dp).height(16.dp), cornerRadius = 4.dp)
        repeat(3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(modifier = Modifier.width(80.dp).height(88.dp), cornerRadius = 8.dp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(modifier = Modifier.width(160.dp).height(14.dp), cornerRadius = 4.dp)
                    ShimmerBox(modifier = Modifier.width(120.dp).height(11.dp), cornerRadius = 4.dp)
                    ShimmerBox(modifier = Modifier.width(100.dp).height(11.dp), cornerRadius = 4.dp)
                }
            }
        }
    }
}
