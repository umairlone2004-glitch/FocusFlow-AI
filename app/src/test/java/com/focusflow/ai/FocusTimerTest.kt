package com.focusflow.ai

import com.focusflow.ai.domain.focus.FocusTimer
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FocusTimerTest {

    @Test
    fun startsAtConfiguredTotal() {
        val timer = FocusTimer(25 * 60)
        assertThat(timer.remainingSeconds).isEqualTo(1500)
        assertThat(timer.totalSeconds).isEqualTo(1500)
        assertThat(timer.isFinished).isFalse()
        assertThat(timer.progress).isEqualTo(0f)
    }

    @Test
    fun tickDecrementsAndReportsProgress() {
        val timer = FocusTimer(10)
        timer.tick()
        timer.tick()
        assertThat(timer.remainingSeconds).isEqualTo(8)
        assertThat(timer.elapsedSeconds).isEqualTo(2)
        assertThat(timer.progress).isWithin(0.001f).of(0.2f)
    }

    @Test
    fun tickStopsAtZeroAndReportsFinished() {
        val timer = FocusTimer(3)
        repeat(5) { timer.tick() }
        assertThat(timer.remainingSeconds).isEqualTo(0)
        assertThat(timer.isFinished).isTrue()
        assertThat(timer.progress).isEqualTo(1f)
    }

    @Test
    fun resetRestoresTotal() {
        val timer = FocusTimer(5)
        repeat(3) { timer.tick() }
        timer.reset()
        assertThat(timer.remainingSeconds).isEqualTo(5)
    }

    @Test
    fun configureChangesTotalAndResets() {
        val timer = FocusTimer(5)
        repeat(2) { timer.tick() }
        timer.configure(60)
        assertThat(timer.totalSeconds).isEqualTo(60)
        assertThat(timer.remainingSeconds).isEqualTo(60)
    }

    @Test
    fun skipToClampsWithinBounds() {
        val timer = FocusTimer(100)
        timer.skipTo(500)
        assertThat(timer.remainingSeconds).isEqualTo(100)
        timer.skipTo(-5)
        assertThat(timer.remainingSeconds).isEqualTo(0)
    }
}
