package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling all pending task reminders...")
            
            val database = AppDatabase.getDatabase(context)
            val repository = TaskRepository(database.taskDao())
            val scheduler = ReminderScheduler(context)

            // Boot receiver runs in a short window. Launching a safe background task.
            CoroutineScope(Dispatchers.IO).launch {
                val currentTime = System.currentTimeMillis()
                val activeTasks = repository.getActiveReminders(currentTime)
                Log.d("BootReceiver", "Found ${activeTasks.size} active tasks to reschedule.")
                for (task in activeTasks) {
                    scheduler.schedule(task)
                }
            }
        }
    }
}
