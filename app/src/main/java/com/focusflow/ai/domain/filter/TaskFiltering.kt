package com.focusflow.ai.domain.filter

import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.Task

enum class TaskSort(val label: String) {
    DUE_DATE("Due date"),
    PRIORITY("Priority"),
    CREATED("Newest"),
    TITLE("Title")
}

data class TaskQuery(
    val text: String = "",
    val priorities: Set<Priority> = emptySet(),
    val categories: Set<String> = emptySet(),
    val showCompleted: Boolean = true,
    val sort: TaskSort = TaskSort.DUE_DATE
)

/** Pure task search, filtering and sorting so it can be unit-tested directly. */
object TaskFiltering {

    fun apply(tasks: List<Task>, query: TaskQuery): List<Task> {
        var result = tasks

        if (query.text.isNotBlank()) {
            val needle = query.text.trim().lowercase()
            result = result.filter { task ->
                task.title.lowercase().contains(needle) ||
                    task.description.lowercase().contains(needle) ||
                    task.category.lowercase().contains(needle) ||
                    task.tags.any { it.lowercase().contains(needle) }
            }
        }
        if (query.priorities.isNotEmpty()) {
            result = result.filter { it.priority in query.priorities }
        }
        if (query.categories.isNotEmpty()) {
            result = result.filter { it.category in query.categories }
        }
        if (!query.showCompleted) {
            result = result.filter { !it.isCompleted }
        }

        return when (query.sort) {
            TaskSort.DUE_DATE -> result.sortedWith(
                compareBy({ it.isCompleted }, { it.dueDate == null }, { it.dueDate }, { -it.priority.weight })
            )
            TaskSort.PRIORITY -> result.sortedWith(
                compareBy({ it.isCompleted }, { -it.priority.weight }, { it.dueDate == null }, { it.dueDate })
            )
            TaskSort.CREATED -> result.sortedWith(
                compareBy({ it.isCompleted }, { -it.createdAt })
            )
            TaskSort.TITLE -> result.sortedWith(
                compareBy({ it.isCompleted }, { it.title.lowercase() })
            )
        }
    }
}
