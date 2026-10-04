package com.focusflow.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.focusflow.ai.data.local.FocusFlowDatabase
import com.focusflow.ai.data.local.TaskDao
import com.focusflow.ai.data.local.TaskEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskDaoTest {

    private lateinit var database: FocusFlowDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(
        title: String = "Task",
        tags: List<String> = emptyList(),
        completed: Boolean = false,
        projectId: Long? = null
    ) = TaskEntity(
        title = title,
        description = "",
        priority = "MEDIUM",
        dueDateEpochDay = null,
        dueTime = null,
        isCompleted = completed,
        completedAt = null,
        category = "",
        tags = tags,
        projectId = projectId,
        recurrence = "NONE",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Test
    fun insertAndReadBack_persistsTask() = runTest {
        val id = dao.insert(entity(title = "Write report"))
        val loaded = dao.getById(id)
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.title).isEqualTo("Write report")
    }

    @Test
    fun tagsRoundTripThroughConverter() = runTest {
        val id = dao.insert(entity(tags = listOf("study", "urgent")))
        val loaded = dao.getById(id)
        assertThat(loaded!!.tags).containsExactly("study", "urgent").inOrder()
    }

    @Test
    fun observeAllReflectsInsertionsAndDeletions() = runTest {
        val id = dao.insert(entity(title = "One"))
        assertThat(dao.observeAll().first()).hasSize(1)

        dao.deleteById(id)
        assertThat(dao.observeAll().first()).isEmpty()
    }

    @Test
    fun updatePersistsChanges() = runTest {
        val id = dao.insert(entity(title = "Draft"))
        val loaded = dao.getById(id)!!
        dao.update(loaded.copy(title = "Final", isCompleted = true))

        val updated = dao.getById(id)!!
        assertThat(updated.title).isEqualTo("Final")
        assertThat(updated.isCompleted).isTrue()
    }

    @Test
    fun observeByProjectFiltersCorrectly() = runTest {
        dao.insert(entity(title = "In project", projectId = 7L))
        dao.insert(entity(title = "No project"))
        val projectTasks = dao.observeByProject(7L).first()
        assertThat(projectTasks).hasSize(1)
        assertThat(projectTasks.first().title).isEqualTo("In project")
    }
}
