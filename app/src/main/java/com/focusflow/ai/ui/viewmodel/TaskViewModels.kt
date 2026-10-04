package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.filter.TaskFiltering
import com.focusflow.ai.domain.filter.TaskQuery
import com.focusflow.ai.domain.filter.TaskSort
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TaskListUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val query: TaskQuery = TaskQuery(),
    val categories: List<String> = emptyList()
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val query = MutableStateFlow(TaskQuery())

    val uiState: StateFlow<TaskListUiState> =
        combine(taskRepository.observeAll(), query) { tasks, q ->
            TaskListUiState(
                isLoading = false,
                tasks = TaskFiltering.apply(tasks, q),
                query = q,
                categories = tasks.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    fun setText(value: String) = query.update { it.copy(text = value) }

    fun togglePriority(priority: Priority) = query.update {
        it.copy(priorities = if (priority in it.priorities) it.priorities - priority else it.priorities + priority)
    }

    fun toggleCategory(category: String) = query.update {
        it.copy(categories = if (category in it.categories) it.categories - category else it.categories + category)
    }

    fun setShowCompleted(show: Boolean) = query.update { it.copy(showCompleted = show) }

    fun setSort(sort: TaskSort) = query.update { it.copy(sort = sort) }

    fun clearFilters() = query.update { TaskQuery() }

    fun toggleComplete(id: Long) {
        viewModelScope.launch { taskRepository.toggleComplete(id) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { taskRepository.delete(task) }
    }
}

data class TaskEditUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val projectId: Long? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val projects: List<Project> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val titleError: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L

    private val _uiState = MutableStateFlow(TaskEditUiState())
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.get()
            val projects = projectRepository.observeAll()
            val categories = taskRepository.observeAll()
            val existing = if (taskId != 0L) taskRepository.getById(taskId) else null
            _uiState.update { state ->
                val base = state.copy(
                    isLoading = false,
                    isEditing = existing != null,
                    projects = emptyList()
                )
                if (existing != null) {
                    base.copy(
                        title = existing.title,
                        description = existing.description,
                        priority = existing.priority,
                        dueDate = existing.dueDate,
                        dueTime = existing.dueTime.orEmpty(),
                        category = existing.category,
                        tags = existing.tags,
                        projectId = existing.projectId,
                        recurrence = existing.recurrence
                    )
                } else {
                    base.copy(priority = profile?.defaultPriority ?: Priority.MEDIUM)
                }
            }
            launch { projects.collect { list -> _uiState.update { it.copy(projects = list) } } }
            launch {
                categories.collect { list ->
                    _uiState.update {
                        it.copy(availableCategories = list.map { t -> t.category }.filter { c -> c.isNotBlank() }.distinct().sorted())
                    }
                }
            }
        }
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value, titleError = null) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun setPriority(value: Priority) = _uiState.update { it.copy(priority = value) }
    fun setDueDate(value: LocalDate?) = _uiState.update { it.copy(dueDate = value) }
    fun setDueTime(value: String) = _uiState.update { it.copy(dueTime = value) }
    fun setCategory(value: String) = _uiState.update { it.copy(category = value) }
    fun setProject(value: Long?) = _uiState.update { it.copy(projectId = value) }
    fun setRecurrence(value: RecurrenceType) = _uiState.update { it.copy(recurrence = value) }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isEmpty()) return
        _uiState.update { if (clean in it.tags) it else it.copy(tags = it.tags + clean) }
    }

    fun removeTag(tag: String) = _uiState.update { it.copy(tags = it.tags - tag) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = if (taskId != 0L) taskRepository.getById(taskId) else null
            val task = Task(
                id = existing?.id ?: 0L,
                title = state.title.trim(),
                description = state.description.trim(),
                priority = state.priority,
                dueDate = state.dueDate,
                dueTime = state.dueTime.trim().ifBlank { null },
                isCompleted = existing?.isCompleted ?: false,
                completedAt = existing?.completedAt,
                category = state.category.trim(),
                tags = state.tags,
                projectId = state.projectId,
                recurrence = state.recurrence,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
            taskRepository.upsert(task)
            _uiState.update { it.copy(saved = true) }
        }
    }
}

data class TaskDetailUiState(
    val task: Task? = null,
    val projectName: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L

    val uiState: StateFlow<TaskDetailUiState> =
        combine(taskRepository.observeById(taskId), projectRepository.observeAll()) { task, projects ->
            TaskDetailUiState(
                task = task,
                projectName = task?.projectId?.let { id -> projects.firstOrNull { it.id == id }?.name },
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskDetailUiState())

    fun toggleComplete() {
        viewModelScope.launch { taskRepository.toggleComplete(taskId) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            taskRepository.deleteById(taskId)
            onDeleted()
        }
    }
}
