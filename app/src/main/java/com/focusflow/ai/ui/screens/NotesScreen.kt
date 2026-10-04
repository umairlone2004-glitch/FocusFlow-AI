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
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.focusflow.ai.ui.viewmodel.NotesViewModel

@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onAddNote: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { FocusFlowTopBar(title = "Notes", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNote,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New note") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search notes") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.notes.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.StickyNote2,
                    title = "No notes yet",
                    message = "Capture ideas, summaries and reminders.",
                    actionLabel = "New note",
                    onAction = onAddNote
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNote(note.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (note.content.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = note.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.togglePin(note) }) {
                                    Icon(
                                        Icons.Rounded.PushPin,
                                        contentDescription = "Pin note",
                                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.delete(note.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete note")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
