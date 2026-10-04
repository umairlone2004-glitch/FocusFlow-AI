package com.focusflow.ai.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.focusflow.ai.MainActivity
import com.focusflow.ai.R

/** Central helper for notification channels and posting. */
object NotificationHelper {

    const val CHANNEL_REMINDERS = "channel_reminders"
    const val CHANNEL_FOCUS = "channel_focus"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            "Task reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Deadline and event reminders" }
        val focus = NotificationChannel(
            CHANNEL_FOCUS,
            "Focus timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts when a focus session ends" }
        manager.createNotificationChannels(listOf(reminders, focus))
    }

    fun show(context: Context, channelId: String, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

/** Schedules (and cancels) alarm-backed notifications. */
object ReminderScheduler {

    fun schedule(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        text: String,
        channelId: String
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent(context, requestCode, title, text, channelId)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                return
            } catch (_: SecurityException) {
                // Fall through to an inexact alarm below.
            }
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancel(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun pendingIntent(
        context: Context,
        requestCode: Int,
        title: String,
        text: String,
        channelId: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
            putExtra(EXTRA_CHANNEL, channelId)
            putExtra(EXTRA_ID, requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_TEXT = "extra_text"
    const val EXTRA_CHANNEL = "extra_channel"
    const val EXTRA_ID = "extra_id"
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT).orEmpty()
        val channel = intent.getStringExtra(ReminderScheduler.EXTRA_CHANNEL)
            ?: NotificationHelper.CHANNEL_REMINDERS
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_ID, 0)
        NotificationHelper.show(context, channel, id, title, text)
    }
}

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Focus session complete"
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT).orEmpty()
        val id = intent.getIntExtra(ReminderScheduler.EXTRA_ID, 0)
        NotificationHelper.show(context, NotificationHelper.CHANNEL_FOCUS, id, title, text)
    }
}
