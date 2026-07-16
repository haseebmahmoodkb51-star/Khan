package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskReminderApp(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()

    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var filterCompletedOnly by remember { mutableStateOf<Boolean?>(null) } // null = All, false = Pending, true = Completed

    val categories = listOf("All", "Work", "Personal", "Health", "Shopping", "Others")

    // Filter tasks based on selections
    val filteredTasks = tasks.filter { task ->
        val categoryMatch = selectedCategoryFilter == "All" || task.category == selectedCategoryFilter
        val statusMatch = when (filterCompletedOnly) {
            true -> task.isCompleted
            false -> !task.isCompleted
            null -> true
        }
        categoryMatch && statusMatch
    }

    val pendingCount = tasks.count { !it.isCompleted }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showForm = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Dashboard Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Task Manager",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (pendingCount == 0) "All caught up! 🎉" else "You have $pendingCount tasks left for today",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Category Horizontal Filters
            Text(
                text = "Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategoryFilter == category
                    val chipBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        label = "chipBg"
                    )
                    val chipTextColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "chipText"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBgColor)
                            .clickable { selectedCategoryFilter = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = chipTextColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Status Filter Row (All, Pending, Completed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("All", null as Boolean?, "status_all"),
                    Triple("Pending", false, "status_pending"),
                    Triple("Completed", true, "status_completed")
                ).forEach { (label, status, tag) ->
                    val isSelected = filterCompletedOnly == status
                    OutlinedButton(
                        onClick = { filterCompletedOnly = status },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tag)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Empty state",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Tasks Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adding a task or switching your filters.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEdit = {
                                editingTask = task
                                showForm = true
                            },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showForm) {
        TaskFormDialog(
            task = editingTask,
            onDismiss = { showForm = false },
            onSave = { title, description, category, dueDate, hasReminder, reminderTime ->
                if (editingTask == null) {
                    viewModel.addTask(title, description, category, dueDate, hasReminder, reminderTime)
                } else {
                    viewModel.updateTask(
                        editingTask!!.copy(
                            title = title,
                            description = description,
                            category = category,
                            dueDate = dueDate,
                            hasReminder = hasReminder,
                            reminderTime = reminderTime
                        )
                    )
                }
                showForm = false
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = getCategoryColor(task.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) MaterialTheme.colorScheme.outlineVariant
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onToggleComplete() }
                    .testTag("task_complete_check_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Pending",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = categoryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date Time Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Due Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatDate(task.dueDate),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (task.hasReminder) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Reminder Active",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = formatTime(task.reminderTime),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("task_edit_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditCalendar,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("task_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Boolean, Long) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "Work") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: System.currentTimeMillis()) }
    var hasReminder by remember { mutableStateOf(task?.hasReminder ?: false) }
    var reminderTime by remember { mutableStateOf(task?.reminderTime ?: (System.currentTimeMillis() + 15 * 60 * 1000)) }

    val categories = listOf("Work", "Personal", "Health", "Shopping", "Others")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (task == null) "Create Task" else "Edit Task",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("What needs to be done?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Add details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_description_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection Header
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Category Chips Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        val color = getCategoryColor(cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color else color.copy(alpha = 0.1f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) color else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("form_category_$cat")
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else color,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Picker Button
                OutlinedCard(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = dueDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                                dueDate = selectedCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Due Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatDate(dueDate), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Automated Reminder Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasReminder) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = if (hasReminder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text("Automated Reminder", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Schedule push notification", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it },
                        modifier = Modifier.testTag("form_reminder_switch")
                    )
                }

                // Reminder Time Selector
                AnimatedVisibility(visible = hasReminder) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedCard(
                            onClick = {
                                val calendar = Calendar.getInstance().apply { timeInMillis = reminderTime }
                                // Pick date first
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        // Pick time next
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val selectedCal = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, day)
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                reminderTime = selectedCal.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            false
                                        ).show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Alert Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatDateTime(reminderTime), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                            } else {
                                onSave(title, description, category, dueDate, hasReminder, reminderTime)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_save_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Work" -> Color(0xFF1E88E5)      // Modern blue
        "Personal" -> Color(0xFF8E24AA)  // Modern violet
        "Health" -> Color(0xFF43A047)    // Modern green
        "Shopping" -> Color(0xFFFB8C00)  // Modern orange
        else -> Color(0xFF546E7A)        // Blue-grey
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
