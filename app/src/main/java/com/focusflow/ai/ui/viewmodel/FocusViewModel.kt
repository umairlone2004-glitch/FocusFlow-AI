package com.focusflow.ai.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.ai.domain.analytics.AnalyticsCalculator
import com.focusflow.ai.domain.focus.FocusTimer
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.FocusRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.notifications.NotificationHelper
import com.focusflow.ai.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TimerPhase(val label: String) { FOCUS("Focus"), BREAK("Break") }

data class FocusUiState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedSessionsToday: Int = 0,
    val focusMinutesToday: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val history: List<FocusSession> = emptyList(),
    val defaultFocusMinutes: Int = 25,
    val defaultBreakMinutes: Int = 5
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val timer = FocusTimer(25 * 60)
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            profileRepository.observe().collect { profile ->
                if (profile != null) applyProfile(profile)
            }
        }
        viewModelScope.launch {
            focusRepository.observeAll().collect { sessions -> updateStats(sessions) }
        }
    }

    private fun applyProfile(profile: UserProfile) {
        val focusSeconds = profile.defaultFocusMinutes * 60
        _uiState.update { state ->
            val newTotal = if (state.phase == TimerPhase.FOCUS) focusSeconds
            else profile.defaultBreakMinutes * 60
            if (!state.isRunning) {
                timer.configure(newTotal)
                state.copy(
                    totalSeconds = newTotal,
                    remainingSeconds = newTotal,
                    dailyGoalMinutes = profile.dailyFocusGoalMinutes,
                    defaultFocusMinutes = profile.defaultFocusMinutes,
                    defaultBreakMinutes = profile.defaultBreakMinutes
                )
            } else {
                state.copy(
                    dailyGoalMinutes = profile.dailyFocusGoalMinutes,
                    defaultFocusMinutes = profile.defaultFocusMinutes,
                    defaultBreakMinutes = profile.defaultBreakMinutes
                )
            }
        }
    }

    private fun updateStats(sessions: List<FocusSession>) {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                completedSessionsToday = sessions.count { s ->
                    s.isCompleted && AnalyticsCalculator.toLocalDate(s.startTime) == today
                },
                focusMinutesToday = AnalyticsCalculator.focusMinutesForDate(sessions, today),
                history = sessions.take(20)
            )
        }
    }

    fun start() {
        if (_uiState.value.isRunning || _uiState.value.remainingSeconds <= 0) return
        _uiState.update { it.copy(isRunning = true) }
        scheduleBackgroundAlarm()
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive && _uiState.value.isRunning) {
                delay(1_000L)
                if (!_uiState.value.isRunning) break
                val finished = timer.tick()
                _uiState.update { it.copy(remainingSeconds = timer.remainingSeconds) }
                if (finished) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    fun pause() {
        _uiState.update { it.copy(isRunning = false) }
        tickJob?.cancel()
        ReminderScheduler.cancel(context, FOCUS_REQUEST_CODE)
    }

    fun reset() {
        timer.reset()
        _uiState.update { it.copy(remainingSeconds = timer.remainingSeconds, isRunning = false) }
        ReminderScheduler.cancel(context, FOCUS_REQUEST_CODE)
    }

    fun skipPhase() {
        onTimerFinished()
    }

    private fun onTimerFinished() {
        tickJob?.cancel()
        ReminderScheduler.cancel(context, FOCUS_REQUEST_CODE)
        val state = _uiState.value
        if (state.phase == TimerPhase.FOCUS) {
            val minutes = state.totalSeconds / 60
            val startTime = System.currentTimeMillis() - state.totalSeconds * 1000L
            viewModelScope.launch {
                focusRepository.addSession(
                    FocusSession(
                        startTime = startTime,
                        durationMinutes = minutes,
                        isCompleted = true,
                        label = "Focus"
                    )
                )
            }
            NotificationHelper.show(
                context,
                NotificationHelper.CHANNEL_FOCUS,
                FOCUS_NOTIFICATION_ID,
                "Focus session complete",
                "Great work! Time for a ${state.defaultBreakMinutes}-minute break."
            )
            val breakSeconds = state.defaultBreakMinutes * 60
            timer.configure(breakSeconds)
            _uiState.update {
                it.copy(
                    phase = TimerPhase.BREAK,
                    totalSeconds = breakSeconds,
                    remainingSeconds = breakSeconds,
                    isRunning = false
                )
            }
        } else {
            NotificationHelper.show(
                context,
                NotificationHelper.CHANNEL_FOCUS,
                FOCUS_NOTIFICATION_ID,
                "Break finished",
                "Ready to focus again?"
            )
            val focusSeconds = state.defaultFocusMinutes * 60
            timer.configure(focusSeconds)
            _uiState.update {
                it.copy(
                    phase = TimerPhase.FOCUS,
                    totalSeconds = focusSeconds,
                    remainingSeconds = focusSeconds,
                    isRunning = false
                )
            }
        }
    }

    private fun scheduleBackgroundAlarm() {
        val state = _uiState.value
        val triggerAt = System.currentTimeMillis() + state.remainingSeconds * 1000L
        ReminderScheduler.schedule(
            context = context,
            requestCode = FOCUS_REQUEST_CODE,
            triggerAtMillis = triggerAt,
            title = if (state.phase == TimerPhase.FOCUS) "Focus session complete" else "Break finished",
            text = "Open FocusFlow AI to continue.",
            channelId = NotificationHelper.CHANNEL_FOCUS
        )
    }

    companion object {
        const val FOCUS_REQUEST_CODE = 9001
        const val FOCUS_NOTIFICATION_ID = 9001
    }
}
