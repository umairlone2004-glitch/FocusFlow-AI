package com.focusflow.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.EmptyState
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.ProgressRing
import com.focusflow.ai.ui.components.TaskCard
import com.focusflow.ai.ui.viewmodel.ProjectDetailViewModel

@Composable
fun ProjectDetailScreen(
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FocusFlowTopBar(title = state.project?.name ?: "Project", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProgressRing(
                        progress = state.completion,
                        label = "${(state.completion * 100).toInt()}%",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "${state.tasks.count { it.isCompleted }}/${state.tasks.size} tasks complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.tasks.isEmpty()) {
                        EmptyState(
                            icon = Icons.Rounded.Checklist,
                            title = "No tasks yet",
                            message = "Assign tasks to this project from the task editor."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.tasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onClick = { onOpenTask(task.id) },
                                    onToggle = { viewModel.toggleComplete(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
