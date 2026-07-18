package com.example.simmr.feature.cooking

import com.example.simmr.core.model.AppConstants

class CookingTimer(initialSeconds: Int) {
    private val minimumSeconds: Int = initialSeconds.coerceAtLeast(0)
    var totalSeconds: Int = minimumSeconds
        private set
    private var pausedRemaining: Int = totalSeconds
    private var deadlineMillis: Long? = null

    val isRunning: Boolean get() = deadlineMillis != null
    val canSubtractMinute: Boolean get() = totalSeconds > minimumSeconds
    fun remaining(nowMillis: Long = System.currentTimeMillis()): Int {
        val deadline = deadlineMillis ?: return pausedRemaining
        val value = kotlin.math.ceil(
            (deadline - nowMillis).coerceAtLeast(0) / AppConstants.Timer.MILLIS_PER_SECOND.toDouble(),
        ).toInt()
        if (value == 0) {
            pausedRemaining = 0
            deadlineMillis = null
        }
        return value
    }
    fun start(nowMillis: Long = System.currentTimeMillis()) {
        if (pausedRemaining > 0 && deadlineMillis == null) deadlineMillis = nowMillis + pausedRemaining * AppConstants.Timer.MILLIS_PER_SECOND
    }
    fun pause(nowMillis: Long = System.currentTimeMillis()) {
        pausedRemaining = remaining(nowMillis)
        deadlineMillis = null
    }
    fun reset() {
        deadlineMillis = null
        totalSeconds = minimumSeconds
        pausedRemaining = minimumSeconds
    }
    fun addMinute(nowMillis: Long = System.currentTimeMillis()) = adjustMinute(1, nowMillis)
    fun subtractMinute(nowMillis: Long = System.currentTimeMillis()) = adjustMinute(-1, nowMillis)
    private fun adjustMinute(direction: Int, nowMillis: Long) {
        if (direction < 0 && !canSubtractMinute) return
        val running = isRunning
        val adjusted = (remaining(nowMillis) + direction * AppConstants.Timer.SECONDS_PER_MINUTE).coerceAtLeast(0)
        totalSeconds = (totalSeconds + direction * AppConstants.Timer.SECONDS_PER_MINUTE).coerceAtLeast(minimumSeconds)
        pausedRemaining = adjusted
        deadlineMillis = if (running && adjusted > 0) nowMillis + adjusted * AppConstants.Timer.MILLIS_PER_SECOND else null
    }
}
