package com.focusflow.ai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.viewmodel.TaskEditViewModel
import com.focusflow.ai.util.DateUtils
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: TaskEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var projectMenuOpen by remember { mutableStateOf(false) }
    var recurrenceMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDate
                ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.setDueDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = if (state.isEditing) "Edit task" else "New task",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Description") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Priority", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.priority == priority,
                        onClick = { viewModel.setPriority(priority) },
                        label = { Text(priority.label) }
                    )
                }
            }

            Text("Due date", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.DateRange, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text(state.dueDate?.let { DateUtils.formatDate(it) } ?: "Pick a date")
                }
                if (state.dueDate != null) {
                    IconButton(onClick = { viewModel.setDueDate(null) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear date")
                    }
                }
            }

            OutlinedTextField(
                value = state.dueTime,
                onValueChange = viewModel::setDueTime,
                label = { Text("Reminder time (HH:mm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.category,
                onValueChange = viewModel::setCategory,
                label = { Text("Category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    label = { Text("Add tag") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    viewModel.addTag(newTag)
                    newTag = ""
                }) { Icon(Icons.Rounded.Add, contentDescription = "Add tag") }
            }
            if (state.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeTag(tag) },
                            label = { Text("#$tag") },
                            trailingIcon = {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove tag")
                            }
                        )
                    }
                }
            }

            Text("Project", style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedButton(onClick = { projectMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        state.projects.firstOrNull { it.id == state.projectId }?.name ?: "No project"
                    )
                }
                DropdownMenu(expanded = projectMenuOpen, onDismissRequest = { projectMenuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("No project") },
                        onClick = { viewModel.setProject(null); projectMenuOpen = false }
                    )
                    state.projects.forEach { project ->
                        DropdownMenuItem(
                            text = { Text(project.name) },
                            onClick = { viewModel.setProject(project.id); projectMenuOpen = false }
                        )
                    }
                }
            }

            Text("Repeat", style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedButton(onClick = { recurrenceMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(state.recurrence.label)
                }
                DropdownMenu(
                    expanded = recurrenceMenuOpen,
                    onDismissRequest = { recurrenceMenuOpen = false }
                ) {
                    RecurrenceType.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { viewModel.setRecurrence(option); recurrenceMenuOpen = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isEditing) "Save changes" else "Create task")
            }
        }
    }
}
