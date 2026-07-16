package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val taskId = intent.getIntExtra("TASK_ID", 0)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val description = intent.getStringExtra("TASK_DESCRIPTION") ?: "You have a task scheduled for now."

        Log.d("ReminderReceiver", "Received alarm for task $taskId: $title")

        val helper = NotificationHelper(context)
        helper.showNotification(taskId, title, description)
    }
}
