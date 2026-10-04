package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.FocusRepository
import com.focusflow.ai.domain.repository.NoteRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val taskRepository: TaskRepository,
    private val focusRepository: FocusRepository,
    private val noteRepository: NoteRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = profileRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun edit(block: (UserProfile) -> UserProfile) {
        viewModelScope.launch {
            val current = profileRepository.get() ?: UserProfile()
            profileRepository.save(block(current))
        }
    }

    fun updateName(name: String) = edit { it.copy(name = name.trim()) }

    fun updateAvatarColor(hex: String) = edit { it.copy(avatarColorHex = hex) }

    fun updateTheme(mode: ThemeMode) = edit { it.copy(themeMode = mode) }

    fun updateNotifications(enabled: Boolean) = edit { it.copy(notificationsEnabled = enabled) }

    fun updateDailyGoal(minutes: Int) = edit { it.copy(dailyFocusGoalMinutes = minutes) }

    fun updateDefaultPriority(priority: Priority) = edit { it.copy(defaultPriority = priority) }

    fun updateTimerDefaults(focusMinutes: Int, breakMinutes: Int) =
        edit { it.copy(defaultFocusMinutes = focusMinutes, defaultBreakMinutes = breakMinutes) }

    /** Clears all user data and resets the profile. Used by "Reset app data". */
    fun clearAllData() {
        viewModelScope.launch {
            taskRepository.clearAll()
            focusRepository.clearAll()
            noteRepository.observeAll().first().forEach { noteRepository.delete(it.id) }
            projectRepository.observeAll().first().forEach { projectRepository.delete(it.id) }
            profileRepository.save(UserProfile())
        }
    }
}
