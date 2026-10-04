package com.focusflow.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootUiState {
    data object Loading : RootUiState
    data object Onboarding : RootUiState
    data object Ready : RootUiState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = profileRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val themeMode: StateFlow<ThemeMode> = profile
        .map { it?.themeMode ?: ThemeMode.SYSTEM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    init {
        viewModelScope.launch {
            if (profileRepository.get() == null) {
                profileRepository.save(UserProfile())
            }
        }
    }

    fun completeOnboarding(name: String, goalMinutes: Int, themeMode: ThemeMode) {
        viewModelScope.launch {
            val current = profileRepository.get() ?: UserProfile()
            profileRepository.save(
                current.copy(
                    name = name.trim().ifBlank { "Friend" },
                    dailyFocusGoalMinutes = goalMinutes,
                    themeMode = themeMode,
                    onboardingComplete = true
                )
            )
        }
    }
}
