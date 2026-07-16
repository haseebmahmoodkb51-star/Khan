package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // e.g., "Work", "Personal", "Health", "Shopping", "Others"
    val dueDate: Long, // Timestamp for due date
    val hasReminder: Boolean = false,
    val reminderTime: Long = 0L, // Timestamp for automated reminder
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
