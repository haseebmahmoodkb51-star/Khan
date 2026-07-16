package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun getActiveReminders(currentTime: Long): List<Task> {
        return taskDao.getActiveReminders(currentTime)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}
