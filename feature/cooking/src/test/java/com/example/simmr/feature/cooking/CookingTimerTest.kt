package com.example.simmr.feature.cooking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CookingTimerTest {
    @Test fun `timer uses wall clock and survives pauses`() {
        val timer = CookingTimer(120)
        timer.start(1_000)
        assertEquals(90, timer.remaining(31_000))
        timer.pause(31_000)
        assertFalse(timer.isRunning)
        assertEquals(90, timer.remaining(90_000))
        timer.start(100_000)
        assertTrue(timer.isRunning)
        assertEquals(0, timer.remaining(190_000))
        assertFalse(timer.isRunning)
    }

    @Test fun `minute controls update remaining`() {
        val timer = CookingTimer(60)
        assertFalse(timer.canSubtractMinute)
        timer.subtractMinute(0)
        assertEquals(60, timer.remaining(0))
        timer.addMinute(0)
        assertTrue(timer.canSubtractMinute)
        assertEquals(120, timer.remaining(0))
        timer.subtractMinute(0)
        assertEquals(60, timer.remaining(0))
    }

    @Test fun `reset restores recipe duration after adjustment`() {
        val timer = CookingTimer(90)
        timer.addMinute(0)
        assertEquals(150, timer.totalSeconds)
        timer.reset()
        assertEquals(90, timer.totalSeconds)
        assertEquals(90, timer.remaining(0))
    }
}
