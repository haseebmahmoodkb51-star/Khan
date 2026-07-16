package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.Task
import com.example.ui.TaskItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TaskCardScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun task_card_screenshot() {
    val sampleTask = Task(
        id = 1,
        title = "Complete Reminder App",
        description = "Add Room database and custom system AlarmManager logic.",
        category = "Work",
        dueDate = 1784167263045L, // Fixed timestamp for deterministic testing
        hasReminder = true,
        reminderTime = 1784167263045L + 3600000L,
        isCompleted = false
    )
    composeTestRule.setContent { 
        MyApplicationTheme { 
            TaskItem(
                task = sampleTask,
                onToggleComplete = {},
                onEdit = {},
                onDelete = {}
            ) 
        } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/task_card.png")
  }
}
