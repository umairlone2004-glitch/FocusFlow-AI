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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.PriorityBadge
import com.focusflow.ai.ui.components.TagChip
import com.focusflow.ai.ui.viewmodel.TaskDetailViewModel
import com.focusflow.ai.util.DateUtils

@Composable
fun TaskDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val task = state.task

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = "Task details",
                onBack = onBack,
                actions = {
                    if (task != null) {
                        IconButton(onClick = { onEdit(task.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.delete(onBack) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (task == null) {
            Spacer(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityBadge(task.priority)
                    Text(
                        text = if (task.isCompleted) "Completed" else "In progress",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Due", task.dueDate?.let { DateUtils.formatDate(it) } ?: "No due date")
                        DetailRow("Time", task.dueTime ?: "—")
                        DetailRow("Category", task.category.ifBlank { "—" })
                        DetailRow("Project", state.projectName ?: "—")
                        DetailRow("Repeats", task.recurrence.label)
                        DetailRow("Created", DateUtils.formatDate(DateUtils.toLocalDate(task.createdAt)))
                    }
                }
                if (task.tags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        task.tags.forEach { TagChip(it) }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.toggleComplete() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (task.isCompleted) "Mark as not done" else "Mark as complete")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
