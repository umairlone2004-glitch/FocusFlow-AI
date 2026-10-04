package com.focusflow.ai.domain.repository

import com.focusflow.ai.domain.analytics.ProductivitySummary
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.Note
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeAll(): Flow<List<Task>>
    fun observeById(id: Long): Flow<Task?>
    fun observeByProject(projectId: Long): Flow<List<Task>>
    suspend fun getById(id: Long): Task?
    suspend fun upsert(task: Task): Long
    suspend fun delete(task: Task)
    suspend fun deleteById(id: Long)
    suspend fun toggleComplete(id: Long)
    suspend fun clearAll()
}

interface ProjectRepository {
    fun observeAll(): Flow<List<Project>>
    fun observeProgress(): Flow<List<ProjectProgress>>
    suspend fun getById(id: Long): Project?
    suspend fun upsert(project: Project): Long
    suspend fun delete(id: Long)
}

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    suspend fun getById(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun delete(id: Long)
    suspend fun setPinned(id: Long, pinned: Boolean)
}

interface FocusRepository {
    fun observeAll(): Flow<List<FocusSession>>
    suspend fun addSession(session: FocusSession): Long
    suspend fun clearAll()
}

interface EventRepository {
    fun observeAll(): Flow<List<CalendarEvent>>
    suspend fun getById(id: Long): CalendarEvent?
    suspend fun upsert(event: CalendarEvent): Long
    suspend fun delete(id: Long)
}

interface ProfileRepository {
    fun observe(): Flow<UserProfile?>
    suspend fun get(): UserProfile?
    suspend fun save(profile: UserProfile)
}

interface AnalyticsRepository {
    fun observeSummary(): Flow<ProductivitySummary>
}
