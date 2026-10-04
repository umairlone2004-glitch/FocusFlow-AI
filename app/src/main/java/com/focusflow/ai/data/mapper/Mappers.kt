package com.focusflow.ai.data.mapper

import com.focusflow.ai.data.local.EventEntity
import com.focusflow.ai.data.local.FocusSessionEntity
import com.focusflow.ai.data.local.NoteEntity
import com.focusflow.ai.data.local.ProfileEntity
import com.focusflow.ai.data.local.ProjectEntity
import com.focusflow.ai.data.local.TaskEntity
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.Note
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.domain.model.UserProfile
import java.time.LocalDate

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    priority = Priority.fromName(priority),
    dueDate = dueDateEpochDay?.let { LocalDate.ofEpochDay(it) },
    dueTime = dueTime,
    isCompleted = isCompleted,
    completedAt = completedAt,
    category = category,
    tags = tags,
    projectId = projectId,
    recurrence = RecurrenceType.fromName(recurrence),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority.name,
    dueDateEpochDay = dueDate?.toEpochDay(),
    dueTime = dueTime,
    isCompleted = isCompleted,
    completedAt = completedAt,
    category = category,
    tags = tags,
    projectId = projectId,
    recurrence = recurrence.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProjectEntity.toDomain(): Project = Project(id, name, description, colorHex, createdAt)

fun Project.toEntity(): ProjectEntity = ProjectEntity(id, name, description, colorHex, createdAt)

fun NoteEntity.toDomain(): Note = Note(id, title, content, isPinned, createdAt, updatedAt)

fun Note.toEntity(): NoteEntity = NoteEntity(id, title, content, isPinned, createdAt, updatedAt)

fun FocusSessionEntity.toDomain(): FocusSession =
    FocusSession(id, startTime, durationMinutes, isCompleted, label)

fun FocusSession.toEntity(): FocusSessionEntity =
    FocusSessionEntity(id, startTime, durationMinutes, isCompleted, label)

fun EventEntity.toDomain(): CalendarEvent =
    CalendarEvent(id, title, description, LocalDate.ofEpochDay(dateEpochDay), startTime, endTime, colorHex)

fun CalendarEvent.toEntity(): EventEntity =
    EventEntity(id, title, description, date.toEpochDay(), startTime, endTime, colorHex)

fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    name = name,
    onboardingComplete = onboardingComplete,
    avatarColorHex = avatarColorHex,
    themeMode = ThemeMode.entries.firstOrNull { it.name.equals(themeMode, true) } ?: ThemeMode.SYSTEM,
    notificationsEnabled = notificationsEnabled,
    dailyFocusGoalMinutes = dailyFocusGoalMinutes,
    defaultPriority = Priority.fromName(defaultPriority),
    defaultFocusMinutes = defaultFocusMinutes,
    defaultBreakMinutes = defaultBreakMinutes
)

fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = 0,
    name = name,
    onboardingComplete = onboardingComplete,
    avatarColorHex = avatarColorHex,
    themeMode = themeMode.name,
    notificationsEnabled = notificationsEnabled,
    dailyFocusGoalMinutes = dailyFocusGoalMinutes,
    defaultPriority = defaultPriority.name,
    defaultFocusMinutes = defaultFocusMinutes,
    defaultBreakMinutes = defaultBreakMinutes
)
