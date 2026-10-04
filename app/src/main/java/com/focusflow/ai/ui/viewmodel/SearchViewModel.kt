package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.CalendarEvent
import com.focusflow.ai.domain.model.Note
import com.focusflow.ai.domain.model.Project
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.repository.EventRepository
import com.focusflow.ai.domain.repository.NoteRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SearchResults(
    val tasks: List<Task> = emptyList(),
    val projects: List<Project> = emptyList(),
    val notes: List<Note> = emptyList(),
    val events: List<CalendarEvent> = emptyList()
) {
    val isEmpty: Boolean get() = tasks.isEmpty() && projects.isEmpty() && notes.isEmpty() && events.isEmpty()
    val total: Int get() = tasks.size + projects.size + notes.size + events.size
}

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val hasQuery: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    taskRepository: TaskRepository,
    projectRepository: ProjectRepository,
    noteRepository: NoteRepository,
    eventRepository: EventRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        taskRepository.observeAll(),
        projectRepository.observeAll(),
        noteRepository.observeAll(),
        eventRepository.observeAll(),
        query
    ) { tasks, projects, notes, events, rawQuery ->
        val needle = rawQuery.trim().lowercase()
        if (needle.isEmpty()) {
            SearchUiState(query = rawQuery, results = SearchResults(), hasQuery = false)
        } else {
            SearchUiState(
                query = rawQuery,
                hasQuery = true,
                results = SearchResults(
                    tasks = tasks.filter {
                        it.title.lowercase().contains(needle) ||
                            it.description.lowercase().contains(needle) ||
                            it.tags.any { tag -> tag.lowercase().contains(needle) }
                    },
                    projects = projects.filter {
                        it.name.lowercase().contains(needle) || it.description.lowercase().contains(needle)
                    },
                    notes = notes.filter {
                        it.title.lowercase().contains(needle) || it.content.lowercase().contains(needle)
                    },
                    events = events.filter {
                        it.title.lowercase().contains(needle) || it.description.lowercase().contains(needle)
                    }
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun setQuery(value: String) {
        query.value = value
    }
}
