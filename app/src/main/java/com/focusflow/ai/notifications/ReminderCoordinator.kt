package com.focusflow.ai.notifications

import android.content.Context
import com.focusflow.ai.domain.reminder.ReminderService
import com.focusflow.ai.domain.repository.EventRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.TaskRepository
import com.focusflow.ai.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns the current task/event data into `AlarmManager`-backed notifications.
 *
 * Refreshing is intentionally idempotent: every task and event is re-evaluated
 * on each call, so new items, edits, completions, deletions and recurring-task
 * instances are all handled by simply calling [refreshTaskReminders] after any
 * mutation. When notifications are disabled in settings, all reminders are
 * cancelled instead.
 */
@Singleton
class ReminderCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository
) : ReminderService {

    override suspend fun refreshTaskReminders() {
        val enabled = notificationsEnabled()
        val now = System.currentTimeMillis()
        taskRepository.observeAll().first().forEach { task ->
            val requestCode = taskRequestCode(task.id)
            val triggerAt = triggerAt(task.dueDate, task.dueTime)
            if (!enabled || task.isCompleted || triggerAt == null || triggerAt <= now) {
                ReminderScheduler.cancel(context, requestCode)
            } else {
                ReminderScheduler.schedule(
                    context = context,
                    requestCode = requestCode,
                    triggerAtMillis = triggerAt,
                    title = "Task due: ${task.title}",
                    text = task.dueDate?.let { DateUtils.formatDate(it) }.orEmpty(),
                    channelId = NotificationHelper.CHANNEL_REMINDERS
                )
            }
        }
    }

    override suspend fun refreshEventReminders() {
        val enabled = notificationsEnabled()
        val now = System.currentTimeMillis()
        eventRepository.observeAll().first().forEach { event ->
            val requestCode = eventRequestCode(event.id)
            val triggerAt = triggerAt(event.date, event.startTime)
            if (!enabled || triggerAt == null || triggerAt <= now) {
                ReminderScheduler.cancel(context, requestCode)
            } else {
                ReminderScheduler.schedule(
                    context = context,
                    requestCode = requestCode,
                    triggerAtMillis = triggerAt,
                    title = event.title,
                    text = "Starts at ${event.startTime}",
                    channelId = NotificationHelper.CHANNEL_REMINDERS
                )
            }
        }
    }

    private suspend fun notificationsEnabled(): Boolean =
        profileRepository.get()?.notificationsEnabled ?: true

    private fun triggerAt(date: LocalDate?, time: String?): Long? {
        if (date == null) return null
        return date.atTime(parseTime(time))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /** Accepts "HH:mm"; falls back to 09:00 when blank or malformed. */
    private fun parseTime(raw: String?): LocalTime {
        if (raw.isNullOrBlank()) return LocalTime.of(9, 0)
        return runCatching { LocalTime.parse(raw.trim()) }.getOrElse { LocalTime.of(9, 0) }
    }

    private fun taskRequestCode(id: Long) = TASK_BASE + (id % RANGE).toInt()

    private fun eventRequestCode(id: Long) = EVENT_BASE + (id % RANGE).toInt()

    private companion object {
        const val TASK_BASE = 100_000
        const val EVENT_BASE = 700_000
        const val RANGE = 100_000L
    }
}
