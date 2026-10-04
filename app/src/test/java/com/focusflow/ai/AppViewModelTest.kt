package com.focusflow.ai

import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.ui.viewmodel.AppViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Verifies the app-startup logic that decides between the loading, onboarding and
 * ready states: a missing profile row is created, and completing onboarding
 * flips the flag that lets the dashboard render.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()

    @Test
    fun createsDefaultProfileAndStartsOnboarding() = runTest {
        val viewModel = AppViewModel(repository)
        val job = launch { viewModel.profile.collect { } }
        advanceUntilIdle()

        val profile = viewModel.profile.value
        assertThat(profile).isNotNull()
        assertThat(profile!!.onboardingComplete).isFalse()
        assertThat(profile.name).isEmpty()
        job.cancel()
    }

    @Test
    fun completingOnboardingSetsFlagAndGoal() = runTest {
        val viewModel = AppViewModel(repository)
        val job = launch { viewModel.profile.collect { } }
        advanceUntilIdle()

        viewModel.completeOnboarding("Umair", 180, ThemeMode.DARK)
        advanceUntilIdle()

        val profile = viewModel.profile.value!!
        assertThat(profile.onboardingComplete).isTrue()
        assertThat(profile.name).isEqualTo("Umair")
        assertThat(profile.dailyFocusGoalMinutes).isEqualTo(180)
        assertThat(profile.themeMode).isEqualTo(ThemeMode.DARK)
        job.cancel()
    }

    @Test
    fun blankNameFallsBackToDefault() = runTest {
        val viewModel = AppViewModel(repository)
        val job = launch { viewModel.profile.collect { } }
        advanceUntilIdle()

        viewModel.completeOnboarding("   ", 120, ThemeMode.SYSTEM)
        advanceUntilIdle()

        assertThat(viewModel.profile.value!!.name).isEqualTo("Friend")
        job.cancel()
    }
}
