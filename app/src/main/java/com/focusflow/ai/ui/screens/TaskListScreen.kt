package com.focusflow.ai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.filter.TaskSort
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.ui.components.EmptyState
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.TaskCard
import com.focusflow.ai.ui.viewmodel.TaskListViewModel

@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = "Tasks",
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query.text,
                onValueChange = viewModel::setText,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search tasks") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FilterRow(
                selectedPriorities = state.query.priorities,
                showCompleted = state.query.showCompleted,
                sort = state.query.sort,
                onTogglePriority = viewModel::togglePriority,
                onToggleCompleted = { viewModel.setShowCompleted(!state.query.showCompleted) },
                onSortChange = viewModel::setSort
            )

            if (state.tasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Checklist,
                    title = "No tasks found",
                    message = if (state.query.text.isBlank())
                        "Create your first task to get started."
                    else "Try a different search or clear your filters.",
                    actionLabel = if (state.query.text.isBlank()) "New task" else "Clear filters",
                    onAction = { if (state.query.text.isBlank()) onAddTask() else viewModel.clearFilters() }
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

@Composable
private fun FilterRow(
    selectedPriorities: Set<Priority>,
    showCompleted: Boolean,
    sort: TaskSort,
    onTogglePriority: (Priority) -> Unit,
    onToggleCompleted: () -> Unit,
    onSortChange: (TaskSort) -> Unit
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = priority in selectedPriorities,
                onClick = { onTogglePriority(priority) },
                label = { Text(priority.label) }
            )
        }
        FilterChip(
            selected = !showCompleted,
            onClick = onToggleCompleted,
            label = { Text("Hide done") }
        )
        Box {
            FilterChip(
                selected = false,
                onClick = { sortMenuOpen = true },
                label = { Text("Sort: ${sort.label}") },
                leadingIcon = { Icon(Icons.Rounded.Sort, contentDescription = null) }
            )
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                TaskSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortChange(option)
                            sortMenuOpen = false
                        }
                    )
                }
            }
        }
    }
}
