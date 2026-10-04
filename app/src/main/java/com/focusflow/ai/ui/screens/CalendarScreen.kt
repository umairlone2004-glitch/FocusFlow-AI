package com.focusflow.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.ui.components.FocusFlowTopBar
import com.focusflow.ai.ui.components.TaskCard
import com.focusflow.ai.ui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onOpenTask: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEventDialog by remember { mutableStateOf(false) }

    if (showEventDialog) {
        AddEventDialog(
            date = state.selectedDate,
            onDismiss = { showEventDialog = false },
            onConfirm = { title, description, start, end ->
                viewModel.addEvent(title, description, state.selectedDate, start, end)
                showEventDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            FocusFlowTopBar(
                title = state.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    " ${state.month.year}",
                actions = {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showEventDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Event") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MonthGrid(
                month = state.month,
                selectedDate = state.selectedDate,
                tasksByDate = state.tasksByDate,
                eventsByDate = state.eventsByDate,
                onSelect = viewModel::selectDate
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = state.selectedDate.format(
                    java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            state.selectedEvents.forEach { event ->
                EventRow(event)
                Spacer(Modifier.height(8.dp))
            }
            if (state.selectedTasks.isEmpty() && state.selectedEvents.isEmpty()) {
                Text(
                    text = "Nothing scheduled for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.selectedTasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onOpenTask(task.id) },
                    onToggle = {}
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: java.time.YearMonth,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onSelect: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val leading = firstDay.dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()
    val cells: List<LocalDate?> = buildList {
        repeat(leading) { add(null) }
        for (day in 1..daysInMonth) add(month.atDay(day))
        while (size % 7 != 0) add(null)
    }
    val weekdays = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val selected = date == selectedDate
                            val hasItems = (tasksByDate[date]?.isNotEmpty() == true) ||
                                (eventsByDate[date]?.isNotEmpty() == true)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = if (selected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onSelect(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selected) FontWeight.SemiBold
                                        else FontWeight.Normal
                                    )
                                }
                                if (hasItems) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.DateRange, contentDescription = null)
            Spacer(Modifier.padding(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${event.startTime} – ${event.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddEventDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("09:00") }
    var end by remember { mutableStateOf("10:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "On ${date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, description, start, end) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
