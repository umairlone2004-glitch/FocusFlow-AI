package com.focusflow.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.focusflow.ai.data.local.FocusFlowDatabase
import com.focusflow.ai.data.repository.TaskRepositoryImpl
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.RecurrenceType
import com.focusflow.ai.domain.model.Task
import com.focusflow.ai.domain.repository.TaskRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskRepositoryTest {

    private lateinit var database: FocusFlowDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TaskRepositoryImpl(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertThenReadBackPersists() = runTest {
        val id = repository.upsert(
            Task(title = "Study chapter", priority = Priority.HIGH, category = "Study")
        )
        val loaded = repository.getById(id)
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.title).isEqualTo("Study chapter")
        assertThat(loaded.priority).isEqualTo(Priority.HIGH)
    }

    @Test
    fun toggleCompleteMarksCompletedAndSetsTimestamp() = runTest {
        val id = repository.upsert(Task(title = "Task"))
        repository.toggleComplete(id)
        val task = repository.getById(id)!!
        assertThat(task.isCompleted).isTrue()
        assertThat(task.completedAt).isNotNull()

        repository.toggleComplete(id)
        assertThat(repository.getById(id)!!.isCompleted).isFalse()
    }

    @Test
    fun completingRecurringTaskCreatesNextInstance() = runTest {
        val id = repository.upsert(
            Task(
                title = "Daily review",
                dueDate = LocalDate.now(),
                recurrence = RecurrenceType.DAILY
            )
        )
        repository.toggleComplete(id)

        val all = repository.observeAll().first()
        assertThat(all).hasSize(2)
        val next = all.first { it.id != id }
        assertThat(next.isCompleted).isFalse()
        assertThat(next.dueDate).isNotNull()
        assertThat(next.dueDate!!.isAfter(LocalDate.now())).isTrue()
    }

    @Test
    fun deleteRemovesTask() = runTest {
        val id = repository.upsert(Task(title = "Temp"))
        repository.deleteById(id)
        assertThat(repository.observeAll().first()).isEmpty()
    }

    @Test
    fun clearAllEmptiesDatabase() = runTest {
        repository.upsert(Task(title = "One"))
        repository.upsert(Task(title = "Two"))
        repository.clearAll()
        assertThat(repository.observeAll().first()).isEmpty()
    }
}
