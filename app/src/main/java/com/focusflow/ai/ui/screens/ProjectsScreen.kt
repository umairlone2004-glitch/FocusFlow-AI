package com.focusflow.ai.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.ui.components.EmptyState
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.viewmodel.ProjectsViewModel

@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    onOpenProject: (Long) -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.create(name, description, "#4C5FD5")
                    name = ""
                    description = ""
                    showDialog = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = { FocusFlowTopBar(title = "Projects", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New project") }
            )
        }
    ) { padding ->
        if (state.projects.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Folder,
                title = "No projects yet",
                message = "Group related tasks into a project to track progress.",
                modifier = Modifier.padding(padding),
                actionLabel = "New project",
                onAction = { showDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.projects, key = { it.project.id }) { progress ->
                    ProjectRow(
                        progress = progress,
                        onClick = { onOpenProject(progress.project.id) },
                        onDelete = { viewModel.delete(progress.project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectRow(
    progress: ProjectProgress,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete project")
                }
            }
            if (progress.project.description.isNotBlank()) {
                Text(
                    text = progress.project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.completion },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${progress.completedTasks}/${progress.totalTasks} tasks complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
