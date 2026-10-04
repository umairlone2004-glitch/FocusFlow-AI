package com.focusflow.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class,
        NoteEntity::class,
        FocusSessionEntity::class,
        EventEntity::class,
        ProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun noteDao(): NoteDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun eventDao(): EventDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val NAME = "focusflow.db"
    }
}
