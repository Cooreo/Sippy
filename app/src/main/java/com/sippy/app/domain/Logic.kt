package com.sippy.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone
import kotlin.math.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.delay

enum class Species(val label: String) {
    DROPLET("Droplet"), SPROUT("Sprout"), KITTY("Kitty"), BLOBBY("Blobby")
}

enum class Mood(val label: String) {
    SLEEPY("Sleepy"), THIRSTY("Thirsty"), CONTENT("Content"), HAPPY("Happy"), DANCING("Dancing")
}

enum class GrowthStage(val label: String, val minPoints: Int) {
    BABY("Baby", 0), YOUNG("Young", 250), GROWN("Grown", 1000), ELDER("Elder", 3000)
}

fun stageFor(points: Int): GrowthStage = GrowthStage.entries
    .sortedByDescending { it.minPoints }
    .first { points >= it.minPoints }

object Rules {
    const val MAX_FRIENDS = 8
    const val TARGET_BONUS_POINTS = 250
    const val MIN_GOAL_ML = 250
    const val MAX_GOAL_ML = 10000

    fun canAddFriend(currentCount: Int) = currentCount < MAX_FRIENDS
    fun canDeleteFriend(currentCount: Int) = currentCount > 1

    fun clampGoal(goal: Int) = goal.coerceIn(MIN_GOAL_ML, MAX_GOAL_ML)

    /** Active friend earns one point per ml plus a once-per-day target bonus. */
    fun pointsFor(ml: Int, totalAfter: Int, goal: Int, targetAlreadyHit: Boolean): Int =
        maxOf(0, ml) + if (!targetAlreadyHit && totalAfter >= goal) TARGET_BONUS_POINTS else 0
}

object MoodCalculator {
    /**
     * Pace assumes a waking window; before it begins there is no expected progress.
     * Sleepy wins only before the user has had time to wake up and nothing is logged.
     */
    fun mood(
        totalMl: Int, goalMl: Int, nowMillis: Long, zone: TimeZone,
        wakeStartHour: Int = 7, wakeEndHour: Int = 22
    ): Mood {
        if (goalMl <= 0) return Mood.CONTENT
        val hour = hourOfDay(nowMillis, zone)
        if (totalMl <= 0 && hour < wakeStartHour + 1) return Mood.SLEEPY
        if (totalMl >= goalMl) return Mood.DANCING
        val diff = progress(totalMl, goalMl) - pace(hour, wakeStartHour, wakeEndHour)
        return when {
            diff < -0.15f -> Mood.THIRSTY
            diff > 0.15f -> Mood.HAPPY
            else -> Mood.CONTENT
        }
    }

    fun progress(totalMl: Int, goalMl: Int): Float =
        if (goalMl <= 0) 0f else (totalMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1.5f)

    fun pace(nowMillis: Long, zone: TimeZone, wakeStartHour: Int = 7, wakeEndHour: Int = 22): Float =
        pace(hourOfDay(nowMillis, zone), wakeStartHour, wakeEndHour)

    private fun pace(hour: Float, wakeStartHour: Int, wakeEndHour: Int): Float {
        val start = wakeStartHour.toFloat().coerceIn(0f, 23f)
        val end = wakeEndHour.toFloat().coerceIn((start + 1).coerceAtMost(23f), 24f)
        return ((hour - start) / (end - start)).coerceIn(0f, 1f)
    }

    private fun hourOfDay(nowMillis: Long, zone: TimeZone): Float {
        val t = Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDateTime()
        return t.hour + t.minute / 60f
    }
}

object StreakLogic {
    /** A hit extends the streak only when yesterday was also a hit; otherwise it restarts at 1. */
    fun afterDay(hitToday: Boolean, hitYesterday: Boolean, current: Int, best: Int): Pair<Int, Int> {
        if (!hitToday) return 0 to best
        val next = if (hitYesterday) max(1, current) + 1 else 1
        return next to max(best, next)
    }

    /** Called on a new day: a missed yesterday breaks the chain, a hit keeps it pending. */
    fun onRollover(hitYesterday: Boolean, current: Int, best: Int): Pair<Int, Int> =
        if (!hitYesterday) 0 to best else current to best
}

object DayKeys {
    fun key(nowMillis: Long, zone: TimeZone): String =
        Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDate().toString()

    fun shift(key: String, days: Long): String = LocalDate.parse(key).plusDays(days).toString()

    fun millisAtStartOfDay(key: String, zone: TimeZone): Long =
        LocalDate.parse(key).atStartOfDay(zone.toZoneId()).toInstant().toEpochMilli()

    fun nowInZone(nowMillis: Long, zone: TimeZone) =
        Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDateTime()
}

/** Abstraction that lets Dev Options fake the clock without touching real data. */
interface AppClock {
    fun now(): Long
    fun zone(): TimeZone
    val tick: Flow<Long>
}

class RealClock : AppClock {
    override fun now() = System.currentTimeMillis()
    override fun zone() = TimeZone.getDefault()
    override val tick: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }
}

class FakeClock(private val delegate: AppClock = RealClock()) : AppClock {
    private val offset = java.util.concurrent.atomic.AtomicLong(0L)
    private val enabled = java.util.concurrent.atomic.AtomicBoolean(false)
    private val _tick = kotlinx.coroutines.flow.MutableStateFlow(delegate.now())
    override val tick: Flow<Long> = merge(delegate.tick, _tick)
    override fun now(): Long = delegate.now() + if (enabled.get()) offset.get() else 0L
    override fun zone(): TimeZone = delegate.zone()
    fun setEnabled(value: Boolean) { enabled.set(value); _tick.value = now() }
    fun isEnabled(): Boolean = enabled.get()
    fun setOffsetHours(hours: Int) {
        offset.set(hours * 3_600_000L)
        _tick.value = now()
    }
    fun offsetHours(): Int = (offset.get() / 3_600_000L).toInt()
}

/** Formatting helpers shared by screens; strings are templates resolved in the UI. */
object AmountFormat {
    fun display(ml: Int, units: String): String = when (units) {
        "oz" -> {
            val oz = ml / 29.5735f
            if (oz >= 10f) "%.0f fl oz".format(oz) else "%.1f fl oz".format(oz)
        }
        else -> "$ml ml"
    }

    fun mlFromDisplayOz(oz: Double): Int = (oz * 29.5735).toInt()
}
