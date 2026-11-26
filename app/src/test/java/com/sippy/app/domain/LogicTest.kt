package com.sippy.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class LogicTest {
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    private fun at(hour: Int, minute: Int = 0): Long {
        val cal = java.util.Calendar.getInstance(utc)
        cal.set(2026, java.util.Calendar.JUNE, 15, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun sleepyBeforeWakeWithNoDrinks() {
        assertEquals(Mood.SLEEPY, MoodCalculator.mood(0, 2000, at(6), utc))
    }

    @Test
    fun thirstyWhenBehindPace() {
        // 18:00 is ~79% of a 07:00-22:00 window; 20% progress is far behind.
        assertEquals(Mood.THIRSTY, MoodCalculator.mood(400, 2000, at(18), utc))
    }

    @Test
    fun contentWhenOnPace() {
        // 13:00 is 40% of the window; 800/2000 is exactly 40%.
        assertEquals(Mood.CONTENT, MoodCalculator.mood(800, 2000, at(13), utc))
    }

    @Test
    fun happyWhenAheadOfPace() {
        assertEquals(Mood.HAPPY, MoodCalculator.mood(1200, 2000, at(10), utc))
    }

    @Test
    fun dancingWhenGoalReached() {
        assertEquals(Mood.DANCING, MoodCalculator.mood(2000, 2000, at(9), utc))
    }

    @Test
    fun streakExtendsOnlyFromConsecutiveDays() {
        assertEquals(Pair(4, 5), StreakLogic.afterDay(true, true, 3, 5))
        assertEquals(Pair(1, 5), StreakLogic.afterDay(true, false, 3, 5))
        assertEquals(Pair(0, 5), StreakLogic.afterDay(false, true, 3, 5))
    }

    @Test
    fun rolloverBreaksStreakAfterMissedDay() {
        assertEquals(Pair(0, 7), StreakLogic.onRollover(false, 5, 7))
        assertEquals(Pair(5, 7), StreakLogic.onRollover(true, 5, 7))
    }

    @Test
    fun dayKeysRollOverAndShiftAcrossMonthBoundary() {
        val day = DayKeys.key(at(23, 59), utc)
        assertEquals("2026-06-15", day)
        assertEquals("2026-06-16", DayKeys.shift(day, 1))
        assertEquals("2026-07-01", DayKeys.shift("2026-06-30", 1))
    }

    @Test
    fun growthStageThresholds() {
        assertEquals(GrowthStage.BABY, stageFor(0))
        assertEquals(GrowthStage.BABY, stageFor(249))
        assertEquals(GrowthStage.YOUNG, stageFor(250))
        assertEquals(GrowthStage.GROWN, stageFor(1000))
        assertEquals(GrowthStage.ELDER, stageFor(3000))
        assertEquals(GrowthStage.ELDER, stageFor(99999))
    }

    @Test
    fun friendLimitAndLastFriendProtection() {
        assertTrue(Rules.canAddFriend(7))
        assertFalse(Rules.canAddFriend(8))
        assertTrue(Rules.canDeleteFriend(2))
        assertFalse(Rules.canDeleteFriend(1))
    }

    @Test
    fun pointsIncludeOncePerDayTargetBonus() {
        assertEquals(250, Rules.pointsFor(250, 250, 2000, false))
        assertEquals(500, Rules.pointsFor(250, 2000, 2000, false))
        assertEquals(250, Rules.pointsFor(250, 2250, 2000, true))
    }

    @Test
    fun goalIsClamped() {
        assertEquals(250, Rules.clampGoal(1))
        assertEquals(10000, Rules.clampGoal(50000))
        assertEquals(2000, Rules.clampGoal(2000))
    }
}
