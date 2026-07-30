package com.yourname.icepacklist.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val savedKey by viewModel.savedKey.collectAsState()
    var inputKey by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }

    // Pre-fill field if a key is already saved
    LaunchedEffect(savedKey) {
        if (inputKey.isEmpty() && savedKey != null) {
            inputKey = savedKey!!
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TMDB API Key",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Enter your TMDB v3 API key. Get one free at themoviedb.org.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it },
            label = { Text("API Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (keyVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        imageVector = if (keyVisible) Icons.Default.VisibilityOff
                                      else Icons.Default.Visibility,
                        contentDescription = if (keyVisible) "Hide key" else "Show key"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (inputKey.isNotBlank()) viewModel.saveKey(inputKey) }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.saveKey(inputKey) },
                enabled = inputKey.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Key")
            }

            OutlinedButton(
                onClick = {
                    viewModel.clearKey()
                    inputKey = ""
                },
                enabled = savedKey != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }

        if (savedKey != null) {
            Text(
                text = "✓ Key saved",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
