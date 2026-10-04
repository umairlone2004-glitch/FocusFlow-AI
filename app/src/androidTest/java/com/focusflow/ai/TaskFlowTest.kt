package com.focusflow.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.focusflow.ai.domain.model.UserProfile
import com.focusflow.ai.domain.repository.ProfileRepository
import com.focusflow.ai.domain.repository.TaskRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import javax.inject.Inject

/**
 * End-to-end Compose tests for the two critical flows: finishing onboarding and
 * creating a task. Data is reset before each test so results are deterministic
 * regardless of what a previous run left in the on-device database.
 */
@HiltAndroidTest
class TaskFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var taskRepository: TaskRepository

    /** Runs after Hilt injection but before the activity is launched. */
    @get:Rule(order = 1)
    val resetDataRule = object : ExternalResource() {
        override fun before() {
            runBlocking {
                taskRepository.clearAll()
                profileRepository.save(UserProfile(onboardingComplete = false))
            }
        }
    }

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun completeOnboarding() {
        awaitText("Continue")
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Get started").performClick()
        awaitText("New task")
    }

    @Test
    fun completeOnboardingReachesDashboard() {
        completeOnboarding()
        composeRule.onNodeWithText("New task").assertIsDisplayed()
    }

    @Test
    fun createTaskShowsItOnDashboard() {
        completeOnboarding()

        composeRule.onNodeWithText("New task").performClick()
        awaitText("Create task")

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Buy groceries")
        composeRule.onNodeWithText("Create task").performClick()

        awaitText("Buy groceries")
    }
}
