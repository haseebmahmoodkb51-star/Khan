package com.example.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.Task

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(task: Task) {
        if (alarmManager == null) return
        if (!task.hasReminder || task.reminderTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            putExtra("TASK_DESCRIPTION", task.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Allows alarm to run even in Doze mode (energy saving)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.reminderTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    task.reminderTime,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled alarm for task ${task.id} at ${task.reminderTime}")
        } catch (e: SecurityException) {
            // Fallback to standard inexact alarm if exact alarm permission is denied or restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                task.reminderTime,
                pendingIntent
            )
            Log.e("ReminderScheduler", "Exact alarm security exception, fell back to standard alarm: ${e.message}")
        }
    }

    fun cancel(task: Task) {
        if (alarmManager == null) return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ReminderScheduler", "Cancelled alarm for task ${task.id}")
        }
    }
}
