package com.sippy.app.data

import com.sippy.app.domain.AppClock
import com.sippy.app.domain.DayKeys
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Rules
import com.sippy.app.domain.Species
import com.sippy.app.domain.StreakLogic
import com.sippy.app.domain.stageFor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

sealed interface AppEvent {
    data class Celebrate(val friendName: String) : AppEvent
    data class LevelUp(val friendName: String, val stage: GrowthStage) : AppEvent
    data class Streak(val days: Int) : AppEvent
    data class Message(val text: String) : AppEvent
}

class WaterRepository(
    private val dao: SippyDao,
    private val settings: SettingsRepository,
    private val clock: AppClock
) {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    val friends = dao.observeFriends()
    fun drinks(day: String) = dao.drinksForDay(day)
    fun total(day: String) = dao.totalForDay(day)
    fun history(limit: Int) = dao.recentStats(limit)

    suspend fun onboard(name: String, species: Species, goalMl: Int) {
        val id = dao.insertFriend(
            FriendEntity(
                name = name.trim().ifBlank { "Sippy" },
                species = species.name,
                createdAt = clock.now(),
                lastActiveAt = clock.now(),
                sortOrder = dao.nextSortOrder()
            )
        )
        settings.setGoal(Rules.clampGoal(goalMl))
        settings.setActiveFriend(id)
        settings.setOnboarding(true)
        settings.setProcessedDay(today())
    }

    suspend fun addFriend(name: String, species: Species): String? {
        if (!Rules.canAddFriend(dao.friendCount())) return "You can have up to ${Rules.MAX_FRIENDS} friends."
        dao.insertFriend(
            FriendEntity(
                name = name.trim().ifBlank { "Friend" },
                species = species.name,
                createdAt = clock.now(),
                lastActiveAt = clock.now(),
                sortOrder = dao.nextSortOrder()
            )
        )
        return null
    }

    suspend fun renameFriend(id: Long, name: String) {
        val friend = dao.friend(id) ?: return
        dao.updateFriend(friend.copy(name = name.trim().ifBlank { friend.name }))
    }

    suspend fun deleteFriend(id: Long): String? {
        if (!Rules.canDeleteFriend(dao.friendCount())) return "Keep at least one friend."
        dao.deleteFriend(id)
        val current = settings.snapshot()
        if (current.activeFriendId == id) {
            settings.setActiveFriend(allFriends().firstOrNull()?.id ?: -1L)
        }
        return null
    }

    suspend fun setActiveFriend(id: Long) {
        val friend = dao.friend(id) ?: return
        dao.updateFriend(friend.copy(lastActiveAt = clock.now()))
        settings.setActiveFriend(id)
    }

    suspend fun logWater(amountMl: Int): LogOutcome {
        val ml = amountMl.coerceAtLeast(1)
        val cfg = settings.snapshot()
        val day = today()
        val active = dao.friend(cfg.activeFriendId) ?: allFriends().firstOrNull()
        val friendId = active?.id ?: -1L
        val before = dao.dayStats(day)?.totalMl ?: dao.sumForDay(day)
        val hitBefore = before >= cfg.goalMl
        val after = before + ml
        val hitAfter = after >= cfg.goalMl

        dao.insertDrink(DrinkEntity(dayKey = day, amountMl = ml, timestamp = clock.now(), friendId = friendId))
        dao.upsertDayStats(DayStatsEntity(day, after, hitAfter))

        var levelUp: GrowthStage? = null
        if (active != null) {
            val stageBefore = stageFor(active.growthPoints)
            val gained = Rules.pointsFor(ml, after, cfg.goalMl, hitBefore)
            val updated = active.copy(growthPoints = active.growthPoints + gained)
            dao.updateFriend(updated)
            val stageAfter = stageFor(updated.growthPoints)
            if (stageAfter != stageBefore) {
                levelUp = stageAfter
                _events.tryEmit(AppEvent.LevelUp(updated.name, stageAfter))
            }
        }

        var streak = cfg.currentStreak
        if (hitAfter && !hitBefore) {
            val yesterdayHit = dao.dayStats(DayKeys.shift(day, -1))?.targetHit == true
            val result = StreakLogic.afterDay(true, yesterdayHit, cfg.currentStreak, cfg.bestStreak)
            streak = result.first
            settings.setStreaks(result.first, result.second)
            _events.tryEmit(AppEvent.Streak(result.first))
        }
        if (hitAfter && cfg.lastCelebratedDay != day) {
            settings.setCelebratedDay(day)
            _events.tryEmit(AppEvent.Celebrate(active?.name ?: "Your friend"))
        }
        _events.tryEmit(AppEvent.Message("Logged $ml ml"))
        return LogOutcome(celebrate = hitAfter && !hitBefore, levelUp = levelUp, streak = streak)
    }

    data class LogOutcome(val celebrate: Boolean, val levelUp: GrowthStage?, val streak: Int)

    suspend fun undoLast(): Boolean {
        val day = today()
        val last = dao.lastDrink(day) ?: return false
        dao.deleteDrink(last.id)
        recomputeDay(day)
        _events.tryEmit(AppEvent.Message("Last drink removed"))
        return true
    }

    suspend fun deleteDrink(id: Long) {
        val drink = drinksOnce(today()).firstOrNull { it.id == id } ?: return
        dao.deleteDrink(id)
        recomputeDay(drink.dayKey)
        _events.tryEmit(AppEvent.Message("Entry removed"))
    }

    suspend fun ensureDayRollover() {
        val day = today()
        val cfg = settings.snapshot()
        if (cfg.lastProcessedDay == day) return
        val yesterday = DayKeys.shift(day, -1)
        val hitYesterday = dao.dayStats(yesterday)?.targetHit == true
        val result = StreakLogic.onRollover(hitYesterday, cfg.currentStreak, cfg.bestStreak)
        settings.setStreaks(result.first, result.second)
        settings.setProcessedDay(day)
        recomputeDay(day)
        _events.tryEmit(AppEvent.Message("New day: ${day}"))
    }

    suspend fun replayCelebration() {
        val cfg = settings.snapshot()
        _events.tryEmit(AppEvent.Celebrate(activeFriendName(cfg.activeFriendId)))
    }

    /** Dev-only, explicit writes below. */
    suspend fun devSetGrowth(friendId: Long, points: Int, stage: GrowthStage? = null): GrowthStage? {
        val friend = dao.friend(friendId) ?: return null
        val newPoints = stage?.minPoints ?: points.coerceAtLeast(0)
        val before = stageFor(friend.growthPoints)
        dao.updateFriend(friend.copy(growthPoints = newPoints))
        val after = stageFor(newPoints)
        if (after != before) _events.tryEmit(AppEvent.LevelUp(friend.name, after))
        return after
    }

    suspend fun devSetStreaks(current: Int, best: Int) = settings.setStreaks(current, best)

    suspend fun devAddOneOfEachSpecies(): String {
        var added = 0
        for (species in Species.entries) {
            if (dao.friendCount() >= Rules.MAX_FRIENDS) break
            if (allFriends().any { it.species == species.name }) continue
            addFriend("Test ${species.label}", species)
            added++
        }
        return "Added $added test friend(s)"
    }

    suspend fun devRemoveTestFriends(): String {
        val tests = allFriends().filter { it.name.startsWith("Test ") }
        for (friend in tests) {
            if (dao.friendCount() <= 1) break
            dao.deleteFriend(friend.id)
        }
        return "Removed ${tests.size} test friend(s)"
    }

    suspend fun devSeedHistory(days: Int): String {
        val cfg = settings.snapshot()
        val today = today()
        var written = 0
        for (back in days - 1 downTo 1) {
            val day = DayKeys.shift(today, -back.toLong())
            if (dao.dayStats(day) != null) continue
            val total = Random.nextInt(700, 3200)
            val friendId = cfg.activeFriendId
            val base = DayKeys.millisAtStartOfDay(day, clock.zone())
            var remaining = total
            var part = 0
            while (remaining > 0) {
                val amount = minOf(remaining, listOf(150, 250, 500).random())
                dao.insertDrink(
                    DrinkEntity(
                        dayKey = day,
                        amountMl = amount,
                        timestamp = base + (9 + part) * 3_600_000L,
                        friendId = friendId
                    )
                )
                remaining -= amount
                part++
            }
            dao.upsertDayStats(DayStatsEntity(day, total, total >= cfg.goalMl))
            written++
        }
        return "Seeded $written day(s)"
    }

    suspend fun devClearAll() {
        dao.clearDrinks()
        dao.clearStats()
        dao.clearFriends()
        settings.clearAll()
        settings.setProcessedDay(today())
        _events.tryEmit(AppEvent.Message("All data cleared"))
    }

    suspend fun activeFriendName(): String = activeFriendName(settings.snapshot().activeFriendId)

    suspend fun activeFriendName(id: Long): String =
        dao.friend(id)?.name ?: allFriends().firstOrNull()?.name ?: "Your friend"

    private suspend fun recomputeDay(day: String) {
        val total = dao.sumForDay(day)
        val cfg = settings.snapshot()
        dao.upsertDayStats(DayStatsEntity(day, total, total >= cfg.goalMl))
    }

    private fun today() = DayKeys.key(clock.now(), clock.zone())

    private suspend fun allFriends(): List<FriendEntity> = dao.observeFriends().first()

    private suspend fun drinksOnce(day: String): List<DrinkEntity> = dao.drinksForDay(day).first()
}
