package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.ProjectProgress
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.reminder.ReminderService
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectsUiState(
    val isLoading: Boolean = true,
    val projects: List<ProjectProgress> = emptyList()
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val uiState: StateFlow<ProjectsUiState> = projectRepository.observeProgress()
        .map { ProjectsUiState(isLoading = false, projects = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectsUiState())

    fun create(name: String, description: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            projectRepository.upsert(
                Project(name = name.trim(), description = description.trim(), colorHex = colorHex)
            )
        }
    }

    fun rename(project: Project, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { projectRepository.upsert(project.copy(name = newName.trim())) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { projectRepository.delete(id) }
    }
}

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val tasks: List<Task> = emptyList(),
    val completion: Float = 0f
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val reminderService: ReminderService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: Long = savedStateHandle.get<Long>("projectId") ?: 0L

    val uiState: StateFlow<ProjectDetailUiState> =
        combine(
            projectRepository.observeAll(),
            taskRepository.observeByProject(projectId)
        ) { projects, tasks ->
            ProjectDetailUiState(
                isLoading = false,
                project = projects.firstOrNull { it.id == projectId },
                tasks = tasks,
                completion = if (tasks.isEmpty()) 0f
                else tasks.count { it.isCompleted }.toFloat() / tasks.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectDetailUiState())

    fun toggleComplete(id: Long) {
        viewModelScope.launch {
            taskRepository.toggleComplete(id)
            reminderService.refreshTaskReminders()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
            reminderService.refreshTaskReminders()
        }
    }
}
