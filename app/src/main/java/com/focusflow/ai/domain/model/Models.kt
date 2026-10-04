package com.focusflow.ai.domain.model

import java.time.LocalDate

/** Priority of a task, ordered from least to most urgent. */
enum class Priority(val label: String, val weight: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2),
    URGENT("Urgent", 3);

    companion object {
        fun fromName(value: String?): Priority =
            Priority.entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

/** How a task repeats after completion. */
enum class RecurrenceType(val label: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun fromName(value: String?): RecurrenceType =
            RecurrenceType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val projectId: Long? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = !isCompleted && dueDate != null && dueDate.isBefore(LocalDate.now())
}

data class Project(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val colorHex: String = "#4C5FD5",
    val createdAt: Long = System.currentTimeMillis()
)

data class ProjectProgress(
    val project: Project,
    val totalTasks: Int,
    val completedTasks: Int
) {
    val completion: Float
        get() = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()
}

data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FocusSession(
    val id: Long = 0L,
    val startTime: Long,
    val durationMinutes: Int,
    val isCompleted: Boolean,
    val label: String = "Focus"
)

data class CalendarEvent(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val colorHex: String = "#4C5FD5"
)

data class UserProfile(
    val name: String = "",
    val onboardingComplete: Boolean = false,
    val avatarColorHex: String = "#4C5FD5",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val dailyFocusGoalMinutes: Int = 120,
    val defaultPriority: Priority = Priority.MEDIUM,
    val defaultFocusMinutes: Int = 25,
    val defaultBreakMinutes: Int = 5
)
