package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val scheduler: ReminderScheduler

    val allTasks: StateFlow<List<Task>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        scheduler = ReminderScheduler(application)

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        dueDate: Long,
        hasReminder: Boolean,
        reminderTime: Long
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                category = category,
                dueDate = dueDate,
                hasReminder = hasReminder,
                reminderTime = reminderTime
            )
            val generatedId = repository.insertTask(task)
            if (hasReminder) {
                val savedTask = task.copy(id = generatedId.toInt())
                scheduler.schedule(savedTask)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            
            // Cancel and reschedule
            scheduler.cancel(task)
            if (task.hasReminder && !task.isCompleted) {
                scheduler.schedule(task)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.insertTask(updatedTask)

            if (updatedTask.isCompleted) {
                scheduler.cancel(updatedTask)
            } else if (updatedTask.hasReminder) {
                scheduler.schedule(updatedTask)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            scheduler.cancel(task)
            repository.deleteTask(task)
        }
    }
}
