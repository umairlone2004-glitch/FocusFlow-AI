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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.ui.components.EmptyState
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.SectionHeader
import com.focusflow.ai.ui.components.TaskCard
import com.focusflow.ai.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenProject: (Long) -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { FocusFlowTopBar(title = "Search", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search tasks, projects, notes, events") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                !state.hasQuery -> EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "Search everything",
                    message = "Find tasks, projects, notes and events across the app."
                )
                state.results.isEmpty -> EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "No results",
                    message = "Nothing matched \"${state.query}\". Try a different term."
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.results.tasks.isNotEmpty()) {
                        item { SectionHeader(title = "Tasks") }
                        items(state.results.tasks, key = { "t-${it.id}" }) { task ->
                            TaskCard(task = task, onClick = { onOpenTask(task.id) }, onToggle = {})
                        }
                    }
                    if (state.results.projects.isNotEmpty()) {
                        item { SectionHeader(title = "Projects") }
                        items(state.results.projects, key = { "p-${it.id}" }) { project ->
                            SimpleResultRow(project.name, project.description) { onOpenProject(project.id) }
                        }
                    }
                    if (state.results.notes.isNotEmpty()) {
                        item { SectionHeader(title = "Notes") }
                        items(state.results.notes, key = { "n-${it.id}" }) { note ->
                            SimpleResultRow(note.title, note.content) { onOpenNote(note.id) }
                        }
                    }
                    if (state.results.events.isNotEmpty()) {
                        item { SectionHeader(title = "Events") }
                        items(state.results.events, key = { "e-${it.id}" }) { event ->
                            SimpleResultRow(event.title, "${event.startTime} – ${event.endTime}") {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleResultRow(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
