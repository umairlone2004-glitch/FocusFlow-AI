package com.focusflow.ai

import com.focusflow.ai.domain.analytics.AnalyticsCalculator
import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AnalyticsCalculatorTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 4)

    private fun millisAt(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun completionRate_isZeroForEmptyList() {
        assertThat(AnalyticsCalculator.completionRate(emptyList())).isEqualTo(0f)
    }

    @Test
    fun completionRate_countsCompleted() {
        val tasks = listOf(
            Task(title = "a", isCompleted = true),
            Task(title = "b", isCompleted = false),
            Task(title = "c", isCompleted = true),
            Task(title = "d", isCompleted = false)
        )
        assertThat(AnalyticsCalculator.completionRate(tasks)).isEqualTo(0.5f)
        assertThat(AnalyticsCalculator.completedCount(tasks)).isEqualTo(2)
    }

    @Test
    fun streak_countsConsecutiveDaysEndingToday() {
        val days = setOf(
            today.toEpochDay(),
            today.minusDays(1).toEpochDay(),
            today.minusDays(2).toEpochDay()
        )
        assertThat(AnalyticsCalculator.streak(days, today)).isEqualTo(3)
    }

    @Test
    fun streak_allowsGapOfOneDayWhenTodayEmpty() {
        val days = setOf(
            today.minusDays(1).toEpochDay(),
            today.minusDays(2).toEpochDay()
        )
        assertThat(AnalyticsCalculator.streak(days, today)).isEqualTo(2)
    }

    @Test
    fun streak_isZeroWhenNoRecentActivity() {
        val days = setOf(today.minusDays(5).toEpochDay())
        assertThat(AnalyticsCalculator.streak(days, today)).isEqualTo(0)
    }

    @Test
    fun focusMinutesForDate_onlyCountsCompletedSessionsOnDate() {
        val sessions = listOf(
            FocusSession(startTime = millisAt(today), durationMinutes = 25, isCompleted = true),
            FocusSession(startTime = millisAt(today), durationMinutes = 15, isCompleted = false),
            FocusSession(startTime = millisAt(today.minusDays(1)), durationMinutes = 50, isCompleted = true)
        )
        assertThat(AnalyticsCalculator.focusMinutesForDate(sessions, today, zone)).isEqualTo(25)
        assertThat(AnalyticsCalculator.totalFocusMinutes(sessions)).isEqualTo(75)
    }

    @Test
    fun dailySeries_hasSevenPointsAndCountsCompletions() {
        val tasks = listOf(
            Task(title = "a", priority = Priority.LOW, isCompleted = true, completedAt = millisAt(today)),
            Task(title = "b", priority = Priority.LOW, isCompleted = true, completedAt = millisAt(today)),
            Task(title = "c", priority = Priority.LOW, isCompleted = true, completedAt = millisAt(today.minusDays(1)))
        )
        val series = AnalyticsCalculator.dailySeries(tasks, 7, today, zone)
        assertThat(series).hasSize(7)
        assertThat(series.last().value).isEqualTo(2)
        assertThat(series[series.size - 2].value).isEqualTo(1)
    }

    @Test
    fun activityDays_mergesTasksAndSessions() {
        val tasks = listOf(
            Task(title = "a", isCompleted = true, completedAt = millisAt(today))
        )
        val sessions = listOf(
            FocusSession(startTime = millisAt(today.minusDays(2)), durationMinutes = 25, isCompleted = true)
        )
        val days = AnalyticsCalculator.activityDays(tasks, sessions, zone)
        assertThat(days).containsExactly(today.toEpochDay(), today.minusDays(2).toEpochDay())
    }
}
