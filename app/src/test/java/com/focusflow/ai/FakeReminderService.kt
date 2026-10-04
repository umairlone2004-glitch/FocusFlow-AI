package com.focusflow.ai

import com.focusflow.ai.domain.reminder.ReminderService

/** No-op reminder service for ViewModel tests. */
class FakeReminderService : ReminderService {

    var taskRefreshCount = 0
        private set
    var eventRefreshCount = 0
        private set

    override suspend fun refreshTaskReminders() {
        taskRefreshCount++
    }

    override suspend fun refreshEventReminders() {
        eventRefreshCount++
    }
}
