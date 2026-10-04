package com.focusflow.ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.focusflow.ai.data.local.FocusFlowDatabase
import com.focusflow.ai.data.repository.ProfileRepositoryImpl
import com.focusflow.ai.domain.model.Priority
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.ProfileRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers the profile storage path used at app startup: the app inserts a default
 * profile when none exists and then reads it back to decide whether to show
 * onboarding or the dashboard.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProfileRepositoryTest {

    private lateinit var database: FocusFlowDatabase
    private lateinit var repository: ProfileRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProfileRepositoryImpl(database.profileDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun startsEmptySoAppKnowsToCreateDefault() = runTest {
        assertThat(repository.get()).isNull()
        assertThat(repository.observe().first()).isNull()
    }

    @Test
    fun savingDefaultProfileMakesItReadableAndNotOnboarded() = runTest {
        repository.save(UserProfile())

        val loaded = repository.get()
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.onboardingComplete).isFalse()
        assertThat(loaded.dailyFocusGoalMinutes).isEqualTo(120)
    }

    @Test
    fun observeEmitsSavedProfile() = runTest {
        repository.save(UserProfile(name = "Umair", onboardingComplete = true))

        val observed = repository.observe().first()
        assertThat(observed).isNotNull()
        assertThat(observed!!.name).isEqualTo("Umair")
        assertThat(observed.onboardingComplete).isTrue()
    }

    @Test
    fun allProfileFieldsRoundTrip() = runTest {
        repository.save(
            UserProfile(
                name = "Umair",
                onboardingComplete = true,
                avatarColorHex = "#2E7D32",
                themeMode = ThemeMode.DARK,
                notificationsEnabled = false,
                dailyFocusGoalMinutes = 240,
                defaultPriority = Priority.URGENT,
                defaultFocusMinutes = 45,
                defaultBreakMinutes = 15
            )
        )

        val loaded = repository.get()!!
        assertThat(loaded.avatarColorHex).isEqualTo("#2E7D32")
        assertThat(loaded.themeMode).isEqualTo(ThemeMode.DARK)
        assertThat(loaded.notificationsEnabled).isFalse()
        assertThat(loaded.dailyFocusGoalMinutes).isEqualTo(240)
        assertThat(loaded.defaultPriority).isEqualTo(Priority.URGENT)
        assertThat(loaded.defaultFocusMinutes).isEqualTo(45)
        assertThat(loaded.defaultBreakMinutes).isEqualTo(15)
    }

    @Test
    fun savingTwiceUpdatesTheSingleRow() = runTest {
        repository.save(UserProfile(name = "First"))
        repository.save(UserProfile(name = "Second", onboardingComplete = true))

        val loaded = repository.get()!!
        assertThat(loaded.name).isEqualTo("Second")
        assertThat(loaded.onboardingComplete).isTrue()
    }
}
