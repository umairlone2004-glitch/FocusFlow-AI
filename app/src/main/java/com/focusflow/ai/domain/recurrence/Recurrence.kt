package com.focusflow.ai.domain.recurrence

import com.focusflow.ai.domain.model.RecurrenceType
import java.time.LocalDate

/** Computes the next due date for a recurring task. */
object Recurrence {

    fun nextDate(from: LocalDate, type: RecurrenceType): LocalDate = when (type) {
        RecurrenceType.NONE -> from
        RecurrenceType.DAILY -> from.plusDays(1)
        RecurrenceType.WEEKLY -> from.plusWeeks(1)
        RecurrenceType.MONTHLY -> from.plusMonths(1)
    }

    /**
     * When a recurring task is completed, produce the next instance's due date,
     * advancing until it lands strictly after [today] so overdue recurring tasks
     * do not immediately reappear as overdue.
     */
    fun nextDueAfter(today: LocalDate, currentDue: LocalDate?, type: RecurrenceType): LocalDate {
        if (type == RecurrenceType.NONE) return currentDue ?: today
        var candidate = nextDate(currentDue ?: today, type)
        while (!candidate.isAfter(today)) {
            candidate = nextDate(candidate, type)
        }
        return candidate
    }
}
