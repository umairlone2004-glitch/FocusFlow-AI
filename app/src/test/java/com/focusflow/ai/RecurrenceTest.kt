package com.focusflow.ai

import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.domain.recurrence.Recurrence
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private val base = LocalDate.of(2026, 10, 4)

    @Test
    fun nextDate_dailyAddsOneDay() {
        assertThat(Recurrence.nextDate(base, RecurrenceType.DAILY))
            .isEqualTo(base.plusDays(1))
    }

    @Test
    fun nextDate_weeklyAddsOneWeek() {
        assertThat(Recurrence.nextDate(base, RecurrenceType.WEEKLY))
            .isEqualTo(base.plusWeeks(1))
    }

    @Test
    fun nextDate_monthlyAddsOneMonth() {
        assertThat(Recurrence.nextDate(base, RecurrenceType.MONTHLY))
            .isEqualTo(base.plusMonths(1))
    }

    @Test
    fun nextDate_noneReturnsSameDate() {
        assertThat(Recurrence.nextDate(base, RecurrenceType.NONE)).isEqualTo(base)
    }

    @Test
    fun nextDueAfter_advancesPastTodayForOverdueDaily() {
        val today = LocalDate.of(2026, 10, 10)
        val due = LocalDate.of(2026, 10, 1)
        val next = Recurrence.nextDueAfter(today, due, RecurrenceType.DAILY)
        assertThat(next.isAfter(today)).isTrue()
        assertThat(next).isEqualTo(LocalDate.of(2026, 10, 11))
    }

    @Test
    fun nextDueAfter_handlesNullDueDate() {
        val today = LocalDate.of(2026, 10, 10)
        val next = Recurrence.nextDueAfter(today, null, RecurrenceType.WEEKLY)
        assertThat(next).isEqualTo(LocalDate.of(2026, 10, 17))
    }
}
