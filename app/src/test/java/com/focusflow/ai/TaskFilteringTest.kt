package com.focusflow.ai

import com.focusflow.ai.domain.filter.TaskFiltering
import com.focusflow.ai.domain.filter.TaskQuery
import com.focusflow.ai.domain.filter.TaskSort
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class TaskFilteringTest {

    private val tasks = listOf(
        Task(id = 1, title = "Write report", description = "quarterly", priority = Priority.HIGH,
            category = "Work", tags = listOf("urgent"), dueDate = LocalDate.of(2026, 10, 5)),
        Task(id = 2, title = "Read book", priority = Priority.LOW,
            category = "Study", dueDate = LocalDate.of(2026, 10, 1)),
        Task(id = 3, title = "Gym", priority = Priority.MEDIUM, category = "Health",
            isCompleted = true, dueDate = LocalDate.of(2026, 9, 30))
    )

    @Test
    fun emptyQuery_returnsAllTasks() {
        val result = TaskFiltering.apply(tasks, TaskQuery())
        assertThat(result).hasSize(3)
    }

    @Test
    fun textMatchesTitleDescriptionAndTags() {
        assertThat(TaskFiltering.apply(tasks, TaskQuery(text = "report")).map { it.id })
            .containsExactly(1L)
        assertThat(TaskFiltering.apply(tasks, TaskQuery(text = "quarterly")).map { it.id })
            .containsExactly(1L)
        assertThat(TaskFiltering.apply(tasks, TaskQuery(text = "urgent")).map { it.id })
            .containsExactly(1L)
    }

    @Test
    fun priorityFilterKeepsMatchingPriorities() {
        val result = TaskFiltering.apply(tasks, TaskQuery(priorities = setOf(Priority.HIGH)))
        assertThat(result.map { it.id }).containsExactly(1L)
    }

    @Test
    fun categoryFilterKeepsMatchingCategories() {
        val result = TaskFiltering.apply(tasks, TaskQuery(categories = setOf("Study", "Health")))
        assertThat(result.map { it.id }).containsExactly(2L, 3L)
    }

    @Test
    fun hidingCompletedRemovesCompletedTasks() {
        val result = TaskFiltering.apply(tasks, TaskQuery(showCompleted = false))
        assertThat(result.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun sortByPriorityPutsUrgentFirst() {
        val result = TaskFiltering.apply(tasks, TaskQuery(sort = TaskSort.PRIORITY))
        assertThat(result.first().id).isEqualTo(1L)
    }

    @Test
    fun sortByDueDatePutsEarliestOpenFirst() {
        val result = TaskFiltering.apply(
            tasks,
            TaskQuery(showCompleted = false, sort = TaskSort.DUE_DATE)
        )
        assertThat(result.map { it.id }).containsExactly(2L, 1L).inOrder()
    }
}
