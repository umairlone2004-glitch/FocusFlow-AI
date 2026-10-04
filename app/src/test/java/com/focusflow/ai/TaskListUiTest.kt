package com.focusflow.ai

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.ui.screens.TaskListScreen
import com.focusflow.ai.ui.theme.FocusFlowTheme
import com.focusflow.ai.ui.viewmodel.TaskListViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Compose UI test for the task list: tasks render and the search box filters the
 * list. Uses a real ViewModel over an in-memory repository, so the whole
 * ViewModel + Compose wiring is exercised without an emulator.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class TaskListUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val repository = FakeTaskRepository()
    private val reminders = FakeReminderService()

    @Test
    fun rendersTasksAndFiltersOnSearch() = runBlocking {
        repository.upsert(Task(title = "Write report"))
        repository.upsert(Task(title = "Read book"))
        val viewModel = TaskListViewModel(repository, reminders)

        composeRule.setContent {
            FocusFlowTheme {
                TaskListScreen(
                    onAddTask = {},
                    onOpenTask = {},
                    onOpenSearch = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithText("Write report").assertIsDisplayed()
        composeRule.onNodeWithText("Read book").assertIsDisplayed()

        composeRule.onNode(hasSetTextAction()).performTextInput("report")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Read book").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Write report").assertIsDisplayed()
    }
}
