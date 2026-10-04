package com.focusflow.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.viewmodel.SettingsViewModel

private val avatarColors = listOf(
    "#4C5FD5", "#2E7D32", "#EF6C00", "#C62828", "#6A1B9A", "#00838F"
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#4C5FD5") }
    var initialised by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        val current = profile
        if (current != null && !initialised) {
            name = current.name
            color = current.avatarColorHex
            initialised = true
        }
    }

    Scaffold(topBar = { FocusFlowTopBar(title = "Profile", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(parseColor(color), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                Spacer(Modifier.padding(12.dp))
                Column {
                    Text(
                        text = name.ifBlank { "Your profile" },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Personalise your workspace",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Avatar colour", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                avatarColors.forEach { hex ->
                    val selected = hex == color
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(parseColor(hex), CircleShape)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { color = hex }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.updateName(name)
                    viewModel.updateAvatarColor(color)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save profile") }
        }
    }
}

private fun parseColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF4C5FD5))
