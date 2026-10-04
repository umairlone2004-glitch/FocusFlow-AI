package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.reminder.ReminderService
import com.focusflow.ai.domain.repository.EventRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksByDate: Map<LocalDate, List<Task>> = emptyMap(),
    val eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),
    val selectedTasks: List<Task> = emptyList(),
    val selectedEvents: List<CalendarEvent> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val reminderService: ReminderService
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        taskRepository.observeAll(),
        eventRepository.observeAll(),
        month,
        selectedDate
    ) { tasks, events, currentMonth, selected ->
        val tasksByDate = tasks.filter { it.dueDate != null }.groupBy { it.dueDate!! }
        val eventsByDate = events.groupBy { it.date }
        CalendarUiState(
            isLoading = false,
            month = currentMonth,
            selectedDate = selected,
            tasksByDate = tasksByDate,
            eventsByDate = eventsByDate,
            selectedTasks = tasksByDate[selected].orEmpty(),
            selectedEvents = eventsByDate[selected].orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() = month.value.let { month.value = it.minusMonths(1) }

    fun nextMonth() = month.value.let { month.value = it.plusMonths(1) }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        month.value = YearMonth.from(date)
    }

    fun addEvent(title: String, description: String, date: LocalDate, start: String, end: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            eventRepository.upsert(
                CalendarEvent(
                    title = title.trim(),
                    description = description.trim(),
                    date = date,
                    startTime = start,
                    endTime = end
                )
            )
            reminderService.refreshEventReminders()
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.delete(id)
            reminderService.refreshEventReminders()
        }
    }
}
