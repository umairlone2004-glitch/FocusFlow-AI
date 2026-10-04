package com.focusflow.ai

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.ai.domain.model.ThemeMode
import com.focusflow.ai.ui.screens.OnboardingScreen
import com.focusflow.ai.ui.theme.FocusFlowTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Compose UI test for the onboarding flow. Runs on the JVM under Robolectric so
 * it is fast and deterministic, unlike an emulator-backed test.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class OnboardingFlowUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsWelcomeStepWithContinueButton() {
        composeRule.setContent {
            FocusFlowTheme { OnboardingScreen(onComplete = { _, _, _ -> }) }
        }
        composeRule.onNodeWithText("FocusFlow AI").assertIsDisplayed()
        composeRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun advancingThroughStepsReportsChosenValues() {
        var result: Triple<String, Int, ThemeMode>? = null
        composeRule.setContent {
            FocusFlowTheme {
                OnboardingScreen(onComplete = { name, goal, theme -> result = Triple(name, goal, theme) })
            }
        }

        composeRule.onNodeWithText("Continue").performScrollTo().performClick()   // name -> goal
        composeRule.onNodeWithText("Continue").performScrollTo().performClick()   // goal -> theme
        composeRule.onNodeWithText("Get started").performScrollTo().performClick()

        assertThat(result).isNotNull()
        assertThat(result!!.second).isEqualTo(120)
        assertThat(result!!.third).isEqualTo(ThemeMode.SYSTEM)
    }
}
