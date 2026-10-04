package com.focusflow.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end Compose tests for the critical flows: completing onboarding and
 * creating a task that then appears on the dashboard.
 *
 * The app is offline-first and persists to Room, so the tests tolerate both a
 * fresh install (onboarding shown) and an already-onboarded install.
 */
@HiltAndroidTest
class TaskFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun awaitAnyText(vararg texts: String) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            texts.any { composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() }
        }
    }

    private fun awaitText(text: String) = awaitAnyText(text)

    /** Finishes onboarding if it is showing, then waits for the dashboard. */
    private fun ensureOnDashboard() {
        awaitAnyText("Continue", "New task")
        if (composeRule.onAllNodesWithText("Continue").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Continue").performClick()
            composeRule.onNodeWithText("Continue").performClick()
            composeRule.onNodeWithText("Get started").performClick()
        }
        awaitText("New task")
    }

    @Test
    fun completeOnboardingReachesDashboard() {
        ensureOnDashboard()
        composeRule.onNodeWithText("New task").assertIsDisplayed()
    }

    @Test
    fun createTaskShowsItOnDashboard() {
        ensureOnDashboard()

        composeRule.onNodeWithText("New task").performClick()
        awaitText("Create task")

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Buy groceries")
        composeRule.onNodeWithText("Create task").performClick()

        awaitText("Buy groceries")
    }
}
