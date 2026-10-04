package com.focusflow.ai.data.repository

import com.focusflow.ai.data.local.EventDao
import com.focusflow.ai.data.local.FocusSessionDao
import com.focusflow.ai.data.local.NoteDao
import com.focusflow.ai.data.local.ProfileDao
import com.focusflow.ai.data.local.ProjectDao
import com.focusflow.ai.data.local.TaskDao
import com.focusflow.ai.data.mapper.toDomain
import com.focusflow.ai.data.mapper.toEntity
import com.focusflow.ai.domain.analytics.AnalyticsCalculator
import com.focusflow.ai.domain.analytics.ProductivitySummary
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.Note
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.recurrence.Recurrence
import com.focusflow.ai.domain.repository.AnalyticsRepository
import com.focusflow.ai.domain.repository.EventRepository
import com.focusflow.ai.domain.repository.FocusRepository
import com.focusflow.ai.domain.repository.NoteRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao
) : TaskRepository {

    override fun observeAll(): Flow<List<Task>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Task?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeByProject(projectId: Long): Flow<List<Task>> =
        dao.observeByProject(projectId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Task? = dao.getById(id)?.toDomain()

    override suspend fun upsert(task: Task): Long {
        val now = System.currentTimeMillis()
        val entity = task.copy(updatedAt = now).toEntity()
        return if (task.id == 0L) dao.insert(entity) else {
            dao.update(entity); task.id
        }
    }

    override suspend fun delete(task: Task) = dao.delete(task.toEntity())

    override suspend fun deleteById(id: Long) = dao.deleteById(id)

    override suspend fun toggleComplete(id: Long) {
        val entity = dao.getById(id) ?: return
        val task = entity.toDomain()
        val now = System.currentTimeMillis()
        if (!task.isCompleted) {
            dao.update(entity.copy(isCompleted = true, completedAt = now, updatedAt = now))
            if (task.recurrence != RecurrenceType.NONE) {
                val next = Recurrence.nextDueAfter(LocalDate.now(), task.dueDate, task.recurrence)
                dao.insert(
                    task.copy(
                        id = 0L,
                        isCompleted = false,
                        completedAt = null,
                        dueDate = next,
                        createdAt = now,
                        updatedAt = now
                    ).toEntity()
                )
            }
        } else {
            dao.update(entity.copy(isCompleted = false, completedAt = null, updatedAt = now))
        }
    }

    override suspend fun clearAll() = dao.deleteAll()
}

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao
) : ProjectRepository {

    override fun observeAll(): Flow<List<Project>> =
        projectDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeProgress(): Flow<List<ProjectProgress>> =
        combine(projectDao.observeAll(), taskDao.observeAll()) { projects, tasks ->
            projects.map { project ->
                val related = tasks.filter { it.projectId == project.id }
                ProjectProgress(
                    project = project.toDomain(),
                    totalTasks = related.size,
                    completedTasks = related.count { it.isCompleted }
                )
            }
        }

    override suspend fun getById(id: Long): Project? = projectDao.getById(id)?.toDomain()

    override suspend fun upsert(project: Project): Long {
        val entity = project.toEntity()
        return if (project.id == 0L) projectDao.insert(entity) else {
            projectDao.update(entity); project.id
        }
    }

    override suspend fun delete(id: Long) {
        projectDao.detachTasks(id)
        projectDao.deleteById(id)
    }
}

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Note? = dao.getById(id)?.toDomain()

    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        val entity = note.copy(updatedAt = now).toEntity()
        return if (note.id == 0L) dao.insert(entity) else {
            dao.update(entity); note.id
        }
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun setPinned(id: Long, pinned: Boolean) = dao.setPinned(id, pinned)
}

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val dao: FocusSessionDao
) : FocusRepository {

    override fun observeAll(): Flow<List<FocusSession>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addSession(session: FocusSession): Long = dao.insert(session.toEntity())

    override suspend fun clearAll() = dao.deleteAll()
}

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao
) : EventRepository {

    override fun observeAll(): Flow<List<CalendarEvent>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): CalendarEvent? = dao.getById(id)?.toDomain()

    override suspend fun upsert(event: CalendarEvent): Long {
        val entity = event.toEntity()
        return if (event.id == 0L) dao.insert(entity) else {
            dao.update(entity); event.id
        }
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dao: ProfileDao
) : ProfileRepository {

    override fun observe(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun get(): UserProfile? = dao.get()?.toDomain()

    override suspend fun save(profile: UserProfile) = dao.upsert(profile.toEntity())
}

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val focusRepository: FocusRepository
) : AnalyticsRepository {

    override fun observeSummary(): Flow<ProductivitySummary> =
        combine(taskRepository.observeAll(), focusRepository.observeAll()) { tasks, sessions ->
            AnalyticsCalculator.summarise(tasks, sessions)
        }
}
