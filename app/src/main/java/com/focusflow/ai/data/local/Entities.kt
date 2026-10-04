package com.focusflow.ai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val priority: String,
    val dueDateEpochDay: Long?,
    val dueTime: String?,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val category: String,
    val tags: List<String>,
    val projectId: Long?,
    val recurrence: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val colorHex: String,
    val createdAt: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startTime: Long,
    val durationMinutes: Int,
    val isCompleted: Boolean,
    val label: String
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val dateEpochDay: Long,
    val startTime: String,
    val endTime: String,
    val colorHex: String
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val onboardingComplete: Boolean,
    val avatarColorHex: String,
    val themeMode: String,
    val notificationsEnabled: Boolean,
    val dailyFocusGoalMinutes: Int,
    val defaultPriority: String,
    val defaultFocusMinutes: Int,
    val defaultBreakMinutes: Int
)
