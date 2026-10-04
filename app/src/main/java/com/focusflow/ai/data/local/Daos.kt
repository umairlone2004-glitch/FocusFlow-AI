package com.focusflow.ai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateEpochDay IS NULL, dueDateEpochDay ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY isCompleted ASC, createdAt DESC")
    fun observeByProject(projectId: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId")
    suspend fun countTasks(projectId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND isCompleted = 1")
    suspend fun countCompletedTasks(projectId: Long): Int

    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId")
    suspend fun detachTasks(projectId: Long)
}

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)
}

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    suspend fun getSince(since: Long): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAll()
}

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY dateEpochDay ASC, startTime ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile WHERE id = 0")
    fun observe(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0")
    suspend fun get(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)
}
