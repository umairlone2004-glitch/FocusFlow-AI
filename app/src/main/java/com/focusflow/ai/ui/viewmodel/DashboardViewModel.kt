package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.analytics.AnalyticsCalculator
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.reminder.ReminderService
import com.focusflow.ai.domain.repository.FocusRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val greetingName: String = "",
    val todayTasks: List<Task> = emptyList(),
    val overdueTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val completedToday: Int = 0,
    val totalOpen: Int = 0,
    val focusMinutesToday: Int = 0,
    val focusGoalMinutes: Int = 120,
    val streak: Int = 0,
    val projects: List<ProjectProgress> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    focusRepository: FocusRepository,
    projectRepository: ProjectRepository,
    profileRepository: ProfileRepository,
    private val reminderService: ReminderService
) : ViewModel() {

    init {
        // Re-arm reminders on launch: alarms do not survive a reboot, so this
        // restores any that are still in the future.
        viewModelScope.launch {
            reminderService.refreshTaskReminders()
            reminderService.refreshEventReminders()
        }
    }

    fun toggleComplete(id: Long) {
        viewModelScope.launch {
            taskRepository.toggleComplete(id)
            reminderService.refreshTaskReminders()
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        taskRepository.observeAll(),
        focusRepository.observeAll(),
        projectRepository.observeProgress(),
        profileRepository.observe()
    ) { tasks, sessions, projects, profile ->
        buildState(tasks, sessions, projects, profile?.name.orEmpty(), profile?.dailyFocusGoalMinutes ?: 120)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun buildState(
        tasks: List<Task>,
        sessions: List<FocusSession>,
        projects: List<ProjectProgress>,
        name: String,
        goal: Int
    ): DashboardUiState {
        val today = LocalDate.now()
        val open = tasks.filter { !it.isCompleted }
        return DashboardUiState(
            isLoading = false,
            greetingName = name.ifBlank { "there" },
            todayTasks = open.filter { it.dueDate == today },
            overdueTasks = open.filter { it.dueDate != null && it.dueDate.isBefore(today) },
            upcomingTasks = open.filter { it.dueDate != null && it.dueDate.isAfter(today) }
                .sortedBy { it.dueDate }
                .take(5),
            completedToday = tasks.count {
                it.isCompleted && it.completedAt != null &&
                    AnalyticsCalculator.toLocalDate(it.completedAt) == today
            },
            totalOpen = open.size,
            focusMinutesToday = AnalyticsCalculator.focusMinutesForDate(sessions, today),
            focusGoalMinutes = goal,
            streak = AnalyticsCalculator.streak(AnalyticsCalculator.activityDays(tasks, sessions), today),
            projects = projects
        )
    }
}
