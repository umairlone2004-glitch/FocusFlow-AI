package com.focusflow.ai.domain.focus

/**
 * A small, deterministic countdown state machine for the focus timer.
 * Kept free of Android and coroutine dependencies so it is trivially testable.
 */
class FocusTimer(totalSeconds: Int) {

    var totalSeconds: Int = totalSeconds.coerceAtLeast(0)
        private set

    var remainingSeconds: Int = totalSeconds.coerceAtLeast(0)
        private set

    val isFinished: Boolean get() = remainingSeconds <= 0

    val progress: Float
        get() = if (totalSeconds <= 0) 0f else (totalSeconds - remainingSeconds).toFloat() / totalSeconds

    val elapsedSeconds: Int get() = totalSeconds - remainingSeconds

    fun configure(newTotalSeconds: Int) {
        totalSeconds = newTotalSeconds.coerceAtLeast(0)
        remainingSeconds = totalSeconds
    }

    fun tick(): Boolean {
        if (remainingSeconds > 0) remainingSeconds--
        return isFinished
    }

    fun reset() {
        remainingSeconds = totalSeconds
    }

    fun skipTo(seconds: Int) {
        remainingSeconds = seconds.coerceIn(0, totalSeconds)
    }
}
