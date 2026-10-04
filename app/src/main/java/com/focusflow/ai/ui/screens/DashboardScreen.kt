package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.ui.components.EmptyState
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.SectionHeader
import com.focusflow.ai.ui.components.StatTile
import com.focusflow.ai.ui.components.TaskCard
import com.focusflow.ai.ui.viewmodel.DashboardViewModel
import com.focusflow.ai.util.DateUtils
import java.time.LocalTime

@Composable
fun DashboardScreen(
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenFocus: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = greeting(state.greetingName),
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTask,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New task") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = "Focus today",
                        value = DateUtils.formatMinutes(state.focusMinutesToday),
                        icon = Icons.Rounded.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Day streak",
                        value = state.streak.toString(),
                        icon = Icons.Rounded.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Done today",
                        value = state.completedToday.toString(),
                        icon = Icons.Rounded.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                FocusGoalCard(state.focusMinutesToday, state.focusGoalMinutes)
            }

            if (state.overdueTasks.isNotEmpty()) {
                item { SectionHeader(title = "Overdue") }
                items(state.overdueTasks, key = { "overdue-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onOpenTask(task.id) },
                        onToggle = { viewModel.toggleComplete(task.id) }
                    )
                }
            }

            item { SectionHeader(title = "Today") }
            if (state.todayTasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.CheckCircle,
                        title = "Nothing due today",
                        message = "You're all caught up. Add a task or start a focus session."
                    )
                }
            } else {
                items(state.todayTasks, key = { "today-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onOpenTask(task.id) },
                        onToggle = { viewModel.toggleComplete(task.id) }
                    )
                }
            }

            if (state.upcomingTasks.isNotEmpty()) {
                item { SectionHeader(title = "Upcoming") }
                items(state.upcomingTasks, key = { "upcoming-${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onOpenTask(task.id) },
                        onToggle = { viewModel.toggleComplete(task.id) }
                    )
                }
            }

            if (state.projects.isNotEmpty()) {
                item {
                    SectionHeader(title = "Projects", actionLabel = "View all", onAction = onOpenProjects)
                }
                items(state.projects.take(3), key = { "project-${it.project.id}" }) { progress ->
                    ProjectProgressCard(progress)
                }
            }
        }
    }
}

@Composable
private fun FocusGoalCard(focusMinutes: Int, goalMinutes: Int) {
    val progress = if (goalMinutes <= 0) 0f else (focusMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Daily focus goal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${DateUtils.formatMinutes(focusMinutes)} of ${DateUtils.formatMinutes(goalMinutes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProjectProgressCard(progress: ProjectProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = progress.project.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${(progress.completion * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.completion },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${progress.completedTasks}/${progress.totalTasks} tasks complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun greeting(name: String): String {
    val hour = LocalTime.now().hour
    val part = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    return "$part, $name"
}
