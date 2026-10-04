package com.focusflow.ai.di

import android.content.Context
import androidx.room.Room
import com.focusflow.ai.data.local.EventDao
import com.focusflow.ai.data.local.FocusFlowDatabase
import com.focusflow.ai.data.local.FocusSessionDao
import com.focusflow.ai.data.local.NoteDao
import com.focusflow.ai.data.local.ProfileDao
import com.focusflow.ai.data.local.ProjectDao
import com.focusflow.ai.data.local.TaskDao
import com.focusflow.ai.data.repository.AnalyticsRepositoryImpl
import com.focusflow.ai.data.repository.EventRepositoryImpl
import com.focusflow.ai.data.repository.FocusRepositoryImpl
import com.focusflow.ai.data.repository.NoteRepositoryImpl
import com.focusflow.ai.data.repository.ProfileRepositoryImpl
import com.focusflow.ai.data.repository.ProjectRepositoryImpl
import com.focusflow.ai.data.repository.TaskRepositoryImpl
import com.focusflow.ai.domain.reminder.ReminderService
import com.focusflow.ai.domain.repository.AnalyticsRepository
import com.focusflow.ai.domain.repository.EventRepository
import com.focusflow.ai.domain.repository.FocusRepository
import com.focusflow.ai.domain.repository.NoteRepository
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.ProjectRepository
import com.focusflow.ai.domain.repository.TaskRepository
import com.focusflow.ai.notifications.ReminderCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusFlowDatabase =
        Room.databaseBuilder(context, FocusFlowDatabase::class.java, FocusFlowDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTaskDao(db: FocusFlowDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideProjectDao(db: FocusFlowDatabase): ProjectDao = db.projectDao()

    @Provides
    fun provideNoteDao(db: FocusFlowDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideFocusSessionDao(db: FocusFlowDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideEventDao(db: FocusFlowDatabase): EventDao = db.eventDao()

    @Provides
    fun provideProfileDao(db: FocusFlowDatabase): ProfileDao = db.profileDao()

    @Provides
    @Singleton
    fun provideTaskRepository(impl: TaskRepositoryImpl): TaskRepository = impl

    @Provides
    @Singleton
    fun provideProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository = impl

    @Provides
    @Singleton
    fun provideNoteRepository(impl: NoteRepositoryImpl): NoteRepository = impl

    @Provides
    @Singleton
    fun provideFocusRepository(impl: FocusRepositoryImpl): FocusRepository = impl

    @Provides
    @Singleton
    fun provideEventRepository(impl: EventRepositoryImpl): EventRepository = impl

    @Provides
    @Singleton
    fun provideProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository = impl

    @Provides
    @Singleton
    fun provideAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository = impl

    @Provides
    @Singleton
    fun provideReminderService(impl: ReminderCoordinator): ReminderService = impl
}
