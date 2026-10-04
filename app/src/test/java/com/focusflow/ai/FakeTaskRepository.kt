package com.focusflow.ai

import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake used by ViewModel tests so no Android framework is required. */
class FakeTaskRepository : TaskRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Task>> = tasks

    override fun observeById(id: Long): Flow<Task?> =
        tasks.map { list -> list.firstOrNull { it.id == id } }

    override fun observeByProject(projectId: Long): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.projectId == projectId } }

    override suspend fun getById(id: Long): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun upsert(task: Task): Long {
        val id = if (task.id == 0L) nextId++ else task.id
        val saved = task.copy(id = id)
        tasks.value = tasks.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun delete(task: Task) {
        tasks.value = tasks.value.filterNot { it.id == task.id }
    }

    override suspend fun deleteById(id: Long) {
        tasks.value = tasks.value.filterNot { it.id == id }
    }

    override suspend fun toggleComplete(id: Long) {
        tasks.value = tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    override suspend fun clearAll() {
        tasks.value = emptyList()
    }
}
