package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.ProgressRing
import com.focusflow.ai.ui.components.StatTile
import com.focusflow.ai.ui.viewmodel.FocusViewModel
import com.focusflow.ai.util.DateUtils

@Composable
fun FocusScreen(viewModel: FocusViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = if (state.totalSeconds <= 0) 0f
    else (state.totalSeconds - state.remainingSeconds).toFloat() / state.totalSeconds

    Scaffold(topBar = { FocusFlowTopBar(title = "Focus") }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = state.phase.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ProgressRing(
                    progress = progress,
                    label = DateUtils.formatDuration(state.remainingSeconds),
                    size = 220
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { if (state.isRunning) viewModel.pause() else viewModel.start() }
                    ) {
                        Icon(
                            imageVector = if (state.isRunning) Icons.Rounded.Pause
                            else Icons.Rounded.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text(if (state.isRunning) "Pause" else "Start")
                    }
                    OutlinedButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text("Reset")
                    }
                    OutlinedButton(onClick = { viewModel.skipPhase() }) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = null)
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        label = "Sessions today",
                        value = state.completedSessionsToday.toString(),
                        icon = Icons.Rounded.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Focus today",
                        value = DateUtils.formatMinutes(state.focusMinutesToday),
                        icon = Icons.Rounded.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                val goalProgress = if (state.dailyGoalMinutes <= 0) 0f
                else (state.focusMinutesToday.toFloat() / state.dailyGoalMinutes).coerceIn(0f, 1f)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Daily goal — ${DateUtils.formatMinutes(state.focusMinutesToday)} / " +
                                DateUtils.formatMinutes(state.dailyGoalMinutes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { goalProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (state.history.isNotEmpty()) {
                item {
                    Text(
                        "Recent sessions",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(state.history, key = { it.id }) { session ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = session.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${session.durationMinutes} min • " +
                                    DateUtils.formatDate(DateUtils.toLocalDate(session.startTime)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
