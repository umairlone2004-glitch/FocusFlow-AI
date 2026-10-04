package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.Note
import com.focusflow.ai.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val query: String = ""
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(noteRepository.observeAll(), query) { notes, q ->
        val filtered = if (q.isBlank()) notes else {
            val needle = q.trim().lowercase()
            notes.filter { it.title.lowercase().contains(needle) || it.content.lowercase().contains(needle) }
        }
        NotesUiState(isLoading = false, notes = filtered, query = q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun setQuery(value: String) = query.update { value }

    fun togglePin(note: Note) {
        viewModelScope.launch { noteRepository.setPinned(note.id, !note.isPinned) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { noteRepository.delete(id) }
    }
}

data class NoteEditUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val title: String = "",
    val content: String = "",
    val saved: Boolean = false
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: 0L

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = if (noteId != 0L) noteRepository.getById(noteId) else null
            _uiState.update {
                if (existing != null) {
                    NoteEditUiState(
                        isLoading = false,
                        isEditing = true,
                        title = existing.title,
                        content = existing.content
                    )
                } else {
                    NoteEditUiState(isLoading = false, isEditing = false)
                }
            }
        }
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }

    fun setContent(value: String) = _uiState.update { it.copy(content = value) }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(saved = true) }
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = if (noteId != 0L) noteRepository.getById(noteId) else null
            noteRepository.upsert(
                Note(
                    id = existing?.id ?: 0L,
                    title = state.title.trim().ifBlank { "Untitled note" },
                    content = state.content.trim(),
                    isPinned = existing?.isPinned ?: false,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}
