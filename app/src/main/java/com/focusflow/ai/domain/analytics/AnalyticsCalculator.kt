package com.focusflow.ai.domain.analytics

import com.focusflow.ai.domain.model.FocusSession
import com.focusflow.ai.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** A single labelled data point used by the charts. */
data class SeriesPoint(val label: String, val value: Int)

data class ProductivitySummary(
    val tasksCompleted: Int,
    val tasksCreated: Int,
    val focusMinutes: Int,
    val completionRate: Float,
    val currentStreak: Int,
    val daily: List<SeriesPoint>,
    val weekly: List<SeriesPoint>,
    val monthly: List<SeriesPoint>
)

/**
 * Pure, side-effect free analytics. All date handling takes an explicit zone so
 * the logic can be unit-tested deterministically.
 */
object AnalyticsCalculator {

    fun toLocalDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun completedCount(tasks: List<Task>): Int = tasks.count { it.isCompleted }

    fun completionRate(tasks: List<Task>): Float =
        if (tasks.isEmpty()) 0f else tasks.count { it.isCompleted }.toFloat() / tasks.size

    fun totalFocusMinutes(sessions: List<FocusSession>): Int =
        sessions.filter { it.isCompleted }.sumOf { it.durationMinutes }

    fun focusMinutesForDate(
        sessions: List<FocusSession>,
        date: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): Int = sessions.filter { it.isCompleted && toLocalDate(it.startTime, zone) == date }
        .sumOf { it.durationMinutes }

    /** Consecutive days (ending today or yesterday) that contain at least one activity. */
    fun streak(activityDays: Set<Long>, today: LocalDate): Int {
        var cursor = today
        if (!activityDays.contains(cursor.toEpochDay())) {
            cursor = cursor.minusDays(1)
            if (!activityDays.contains(cursor.toEpochDay())) return 0
        }
        var count = 0
        while (activityDays.contains(cursor.toEpochDay())) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun activityDays(
        tasks: List<Task>,
        sessions: List<FocusSession>,
        zone: ZoneId = ZoneId.systemDefault()
    ): Set<Long> {
        val days = mutableSetOf<Long>()
        tasks.filter { it.isCompleted && it.completedAt != null }
            .forEach { days.add(toLocalDate(it.completedAt!!, zone).toEpochDay()) }
        sessions.filter { it.isCompleted }
            .forEach { days.add(toLocalDate(it.startTime, zone).toEpochDay()) }
        return days
    }

    fun dailySeries(
        tasks: List<Task>,
        days: Int = 7,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<SeriesPoint> {
        val completedDays = tasks.filter { it.isCompleted && it.completedAt != null }
            .groupingBy { toLocalDate(it.completedAt!!, zone).toEpochDay() }
            .eachCount()
        return (days - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            SeriesPoint(label, completedDays[date.toEpochDay()] ?: 0)
        }
    }

    fun weeklySeries(
        tasks: List<Task>,
        weeks: Int = 6,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<SeriesPoint> {
        val completedDays = tasks.filter { it.isCompleted && it.completedAt != null }
            .groupingBy { toLocalDate(it.completedAt!!, zone).toEpochDay() }
            .eachCount()
        val thisWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        return (weeks - 1 downTo 0).map { offset ->
            val start = thisWeekStart.minusWeeks(offset.toLong())
            val total = (0..6).sumOf { completedDays[start.plusDays(it.toLong()).toEpochDay()] ?: 0 }
            SeriesPoint("W${weeks - offset}", total)
        }
    }

    fun monthlySeries(
        tasks: List<Task>,
        months: Int = 6,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<SeriesPoint> {
        val completedDays = tasks.filter { it.isCompleted && it.completedAt != null }
            .groupingBy { toLocalDate(it.completedAt!!, zone) }
            .eachCount()
        return (months - 1 downTo 0).map { offset ->
            val month = today.minusMonths(offset.toLong()).withDayOfMonth(1)
            val value = completedDays.entries.filter { it.key.year == month.year && it.key.month == month.month }
                .sumOf { it.value }
            SeriesPoint(month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), value)
        }
    }

    fun summarise(
        tasks: List<Task>,
        sessions: List<FocusSession>,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): ProductivitySummary = ProductivitySummary(
        tasksCompleted = completedCount(tasks),
        tasksCreated = tasks.size,
        focusMinutes = totalFocusMinutes(sessions),
        completionRate = completionRate(tasks),
        currentStreak = streak(activityDays(tasks, sessions, zone), today),
        daily = dailySeries(tasks, 7, today, zone),
        weekly = weeklySeries(tasks, 6, today, zone),
        monthly = monthlySeries(tasks, 6, today, zone)
    )
}
