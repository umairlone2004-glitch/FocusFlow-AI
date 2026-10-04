package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset app data?") },
            text = { Text("This deletes all tasks, projects, notes and focus history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showResetDialog = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(topBar = { FocusFlowTopBar(title = "Settings", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null)
                    Spacer(Modifier.padding(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            profile?.name?.ifBlank { "Set your name" } ?: "Profile",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Edit profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onOpenProfile) {
                        Text("Edit")
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                    }
                }
            }

            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = profile?.themeMode == mode,
                            onClick = { viewModel.updateTheme(mode) }
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = profile?.themeMode == mode,
                        onClick = { viewModel.updateTheme(mode) }
                    )
                    Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }

            HorizontalDivider()

            Text("Notifications", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable reminders", modifier = Modifier.weight(1f))
                Switch(
                    checked = profile?.notificationsEnabled ?: true,
                    onCheckedChange = { viewModel.updateNotifications(it) }
                )
            }

            HorizontalDivider()

            Text("Focus timer", style = MaterialTheme.typography.titleLarge)
            Text("Daily focus goal", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(60, 120, 180, 240).forEach { minutes ->
                    FilterChip(
                        selected = profile?.dailyFocusGoalMinutes == minutes,
                        onClick = { viewModel.updateDailyGoal(minutes) },
                        label = { Text("${minutes / 60}h") }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Focus duration (minutes)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 50).forEach { minutes ->
                    FilterChip(
                        selected = profile?.defaultFocusMinutes == minutes,
                        onClick = {
                            viewModel.updateTimerDefaults(
                                minutes,
                                profile?.defaultBreakMinutes ?: 5
                            )
                        },
                        label = { Text("$minutes") }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Break duration (minutes)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { minutes ->
                    FilterChip(
                        selected = profile?.defaultBreakMinutes == minutes,
                        onClick = {
                            viewModel.updateTimerDefaults(
                                profile?.defaultFocusMinutes ?: 25,
                                minutes
                            )
                        },
                        label = { Text("$minutes") }
                    )
                }
            }

            HorizontalDivider()

            Text("Tasks", style = MaterialTheme.typography.titleLarge)
            Text("Default priority", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = profile?.defaultPriority == priority,
                        onClick = { viewModel.updateDefaultPriority(priority) },
                        label = { Text(priority.label) }
                    )
                }
            }

            HorizontalDivider()

            Text("Data", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset app data") }

            HorizontalDivider()

            Text("About", style = MaterialTheme.typography.titleLarge)
            Text(
                "FocusFlow AI 1.0.0\nAn offline-first productivity and study manager built with Jetpack Compose.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
