package com.focusflow.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TaskFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun completeOnboardingReachesDashboard() {
        composeRule.onNodeWithText("FocusFlow AI").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Get started").performClick()

        composeRule.onNodeWithText("New task").assertIsDisplayed()
    }

    @Test
    fun createTaskShowsItOnDashboard() {
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Get started").performClick()

        composeRule.onNodeWithText("New task").performClick()
        composeRule.onNodeWithText("Create task").assertIsDisplayed()

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Buy groceries")
        composeRule.onNodeWithText("Create task").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Buy groceries").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
