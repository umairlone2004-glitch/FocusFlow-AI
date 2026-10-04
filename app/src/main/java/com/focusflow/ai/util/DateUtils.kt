package com.focusflow.ai.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun formatDuration(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        val minutes = safe / 60
        val secs = safe % 60
        return String.format(Locale.US, "%02d:%02d", minutes, secs)
    }

    fun formatMinutes(minutes: Int): String {
        val safe = minutes.coerceAtLeast(0)
        return when {
            safe < 60 -> "${safe}m"
            safe % 60 == 0 -> "${safe / 60}h"
            else -> "${safe / 60}h ${safe % 60}m"
        }
    }

    fun formatDate(date: LocalDate): String = date.format(displayFormatter)

    fun toLocalDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun startOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun relativeDueLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val days = date.toEpochDay() - today.toEpochDay()
        return when {
            days < 0 -> "Overdue"
            days == 0L -> "Today"
            days == 1L -> "Tomorrow"
            days <= 7L -> "In $days days"
            else -> formatDate(date)
        }
    }
}
