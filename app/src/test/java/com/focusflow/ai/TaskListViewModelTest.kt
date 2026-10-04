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

    @Test
    fun searchFiltersVisibleTasks() = runTest {
        repository.upsert(Task(title = "Write report", priority = Priority.HIGH))
        repository.upsert(Task(title = "Read book", priority = Priority.LOW))
        val viewModel = TaskListViewModel(repository)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setText("report")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.tasks.map { it.title }).containsExactly("Write report")
        job.cancel()
    }

    @Test
    fun toggleCompleteUpdatesTaskState() = runTest {
        val id = repository.upsert(Task(title = "Task one"))
        val viewModel = TaskListViewModel(repository)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.toggleComplete(id)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.tasks.first().isCompleted).isTrue()
        job.cancel()
    }

    @Test
    fun hidingCompletedRemovesThemFromList() = runTest {
        repository.upsert(Task(title = "Done", isCompleted = true))
        repository.upsert(Task(title = "Open"))
        val viewModel = TaskListViewModel(repository)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setShowCompleted(false)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.tasks.map { it.title }).containsExactly("Open")
        job.cancel()
    }

    @Test
    fun clearFiltersResetsQuery() = runTest {
        repository.upsert(Task(title = "Alpha"))
        val viewModel = TaskListViewModel(repository)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.setText("zzz")
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.tasks).isEmpty()

        viewModel.clearFilters()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.tasks).hasSize(1)
        job.cancel()
    }
}
