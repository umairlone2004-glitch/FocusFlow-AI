package com.focusflow.ai.domain.reminder

/**
 * Schedules and cancels the local notifications the app is responsible for
 * (task deadlines and calendar events). Implementations live in the
 * notification layer; ViewModels depend only on this abstraction so they stay
 * testable without an Android Context.
 */
interface ReminderService {
    /** Re-syncs alarm-backed reminders for every task that has a due date. */
    suspend fun refreshTaskReminders()

    /** Re-syncs alarm-backed reminders for every calendar event. */
    suspend fun refreshEventReminders()
}
