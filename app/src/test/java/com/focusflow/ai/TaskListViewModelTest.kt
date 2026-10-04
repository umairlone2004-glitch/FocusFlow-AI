package com.focusflow.ai

import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.ui.viewmodel.TaskListViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTaskRepository()
    private val reminders = FakeReminderService()

    private fun viewModel() = TaskListViewModel(repository, reminders)

    @Test
    fun searchFiltersVisibleTasks() = runTest {
        repository.upsert(Task(title = "Write report", priority = Priority.HIGH))
        repository.upsert(Task(title = "Read book", priority = Priority.LOW))
        val vm = viewModel()
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        vm.setText("report")
        advanceUntilIdle()

        assertThat(vm.uiState.value.tasks.map { it.title }).containsExactly("Write report")
        job.cancel()
    }

    @Test
    fun toggleCompleteUpdatesTaskStateAndRefreshesReminders() = runTest {
        val id = repository.upsert(Task(title = "Task one"))
        val vm = viewModel()
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        vm.toggleComplete(id)
        advanceUntilIdle()

        assertThat(vm.uiState.value.tasks.first().isCompleted).isTrue()
        assertThat(reminders.taskRefreshCount).isEqualTo(1)
        job.cancel()
    }

    @Test
    fun deletingTaskRefreshesReminders() = runTest {
        val id = repository.upsert(Task(title = "Temporary"))
        val vm = viewModel()
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        vm.delete(repository.getById(id)!!)
        advanceUntilIdle()

        assertThat(vm.uiState.value.tasks).isEmpty()
        assertThat(reminders.taskRefreshCount).isEqualTo(1)
        job.cancel()
    }

    @Test
    fun hidingCompletedRemovesThemFromList() = runTest {
        repository.upsert(Task(title = "Done", isCompleted = true))
        repository.upsert(Task(title = "Open"))
        val vm = viewModel()
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        vm.setShowCompleted(false)
        advanceUntilIdle()

        assertThat(vm.uiState.value.tasks.map { it.title }).containsExactly("Open")
        job.cancel()
    }

    @Test
    fun clearFiltersResetsQuery() = runTest {
        repository.upsert(Task(title = "Alpha"))
        val vm = viewModel()
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        vm.setText("zzz")
        advanceUntilIdle()
        assertThat(vm.uiState.value.tasks).isEmpty()

        vm.clearFilters()
        advanceUntilIdle()
        assertThat(vm.uiState.value.tasks).hasSize(1)
        job.cancel()
    }
}
